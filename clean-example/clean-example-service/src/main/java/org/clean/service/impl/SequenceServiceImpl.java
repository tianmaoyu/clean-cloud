package org.clean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.clean.example.entity.SequenceGenerator;
import org.clean.service.SequenceService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SequenceServiceImpl implements SequenceService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    private static final String SEQUENCE_CACHE_KEY_PREFIX = "sequence:cache:";
    private static final String SEQUENCE_LOCK_KEY_PREFIX = "sequence:lock:";
    private static final String sequence_clean_key_prefix = "sequence:clean:";
    //预生成数量
    private static final int MAX_SEQUENCE_VALUE = 5000;
    //单次最大取数
    private static final int MIN_SEQUENCE_VALUE = 1000;

    /**
     * 获取一个编号
     */
    public String getNextSequence(String bizType) {

        Assert.hasText(bizType, "bizType can not be empty");

        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        // 从缓存中弹出一个编号
        String sequence = redisTemplate.opsForList().rightPop(cacheKey);
        if (sequence != null)  return sequence;

        //数量不够进行填充
        this.refillCache(bizType);

        // 二次获取
        sequence = redisTemplate.opsForList().rightPop(cacheKey);
        if (sequence != null){
            log.error("二次获取单个编号失败 bizType:{}",bizType);
            throw new RuntimeException("二次获取单个编号错误");
        }
        return sequence;
    }

    /**
     * 批量获取编号
     */
    public List<String> getNextSequence(String bizType, int count) {

        Assert.isTrue(count > 0, "count must be greater than 0");
        Assert.isTrue(count <= 1000, "count must be less than or equal to 1000");
        Assert.hasText(bizType, "bizType can not be empty");


        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        //从缓存中弹出需要的数量
        List<String> sequences = redisTemplate.opsForList().rightPop(cacheKey, count);
        int cacheCount = sequences==null ? 0: sequences.size();
        if(cacheCount==count) return sequences;

        //数量不够进行填充
        this.refillCache(bizType);

        //二次获取剩余的
        int needCount = count-cacheCount;
        List<String> _sequences = redisTemplate.opsForList().rightPop(cacheKey, needCount);
        if(CollectionUtils.isEmpty(_sequences)||_sequences.size()< needCount ) {
           log.error("二次批量获取编号失败 bizType:{}",cacheKey);
           throw new RuntimeException("二次批量获取编号错误");
        }
        sequences.addAll(_sequences);

        return sequences;
    }


    /**
     * 补充缓存编号- 并发安全
     */
    public Boolean refillCache(String bizType) {

        String lockkey = SEQUENCE_LOCK_KEY_PREFIX + bizType;
        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        RLock lock = redissonClient.getLock(lockkey);
        try {
            // 尝试加锁，最多等待10秒，锁持有时间30秒，防止死锁
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (isLocked) {

                //缓存数量,需要填充数量
                Long cacheSize = redisTemplate.opsForList().size(cacheKey);
                cacheSize=cacheSize==null?0:cacheSize;
                int fillCount = 2000- cacheSize.intValue();
                //的已经进行填充,并且少量消费,避免重复填充
                if(fillCount<1000) return true;

                List<String> noList = this.generateSequences(bizType, fillCount);
                redisTemplate.opsForList().leftPushAll(cacheKey, noList);

                log.info("补充编号bizType: {}, 剩余数量:{}, 新填充数量: {}", bizType,  cacheSize, fillCount);

                return true;
            }
        }  catch (Exception e) {
            log.error("补充缓存编号bizType错误:{}",cacheKey, e);
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
        return false;
    }


    /**
     * 每日重置 - 并发安全
     * @param bizType
     * @return
     */
    public Boolean  dailyReset (String bizType) {
        this.dailyClearCache(bizType);
        this.refillCache(bizType);
        return true;
    }

    /**
     * 每日凌晨清理重新设置 - 并发安全
     * @param bizType
     * @return
     */
    private Boolean dailyClearCache(String bizType){

        // 使用日期作为标记，确保每天只清理一次
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String markerKey = sequence_clean_key_prefix + today+ ":" +bizType ;
        Boolean alreadyCleaned = redisTemplate.hasKey(markerKey);
        if (Boolean.TRUE.equals(alreadyCleaned)) return true;


        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;
        String lockkey = SEQUENCE_LOCK_KEY_PREFIX + bizType;
        //那些是否要清除 todo

        RLock lock = redissonClient.getLock(lockkey);
        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (isLocked) {

                // 再次检查标记（双重检查）
                alreadyCleaned = redisTemplate.hasKey(markerKey);
                if (Boolean.TRUE.equals(alreadyCleaned)) return true;

                redisTemplate.delete(cacheKey);

                redisTemplate.opsForValue().set(markerKey, "1", 25, TimeUnit.HOURS);

                log.info("成功清理缓存，bizType: {}", bizType);

                return true;
            }
        }  catch (Exception e) {
            log.error("清除缓存失败:{}",cacheKey, e);
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
        return false;
    }


    /**
     * 配置规则变更,清理缓存,填充缓存
     * @param bizType
     * @return
     */
    public Boolean configChangeReset(String bizType){

        //清理
        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;
        redisTemplate.delete(cacheKey);

        //填充
        this.refillCache(bizType);
        return true;
    }

    private List<String> generateSequences(String bizType, int generateCount) {
//        SequenceGenerator generator = sequenceGeneratorMapper.findByBizType(bizType);
        SequenceGenerator generator = new SequenceGenerator();
        if (generator == null) {
            throw new RuntimeException("未找到对应的编号生成器: " + bizType);
        }

        // 获取数据库中的当前值
        long startValue = generator.getCurrentValue();
        int stepSize = Math.max(generateCount, generator.getStepSize());

        // 生成编号,各种规则
        List<String> sequences = new ArrayList<>();
        for (int i = 1; i <= stepSize; i++) {
            long sequenceValue = startValue + i;
            sequences.add(formatSequence(bizType, sequenceValue));
        }
        // 更新数据库
        generator.setCurrentValue(startValue + stepSize);
//        int updated = sequenceGeneratorMapper.updateWithVersion(generator);

        log.info("成功生成一批编号. bizType: {}, 数量: {}, 起始值: {}, 结束值: {}",
                bizType, stepSize, startValue + 1, startValue + stepSize);

        return sequences;
    }

    /**
     * 格式化编号
     */
    private String formatSequence(String bizType, long value) {
        switch (bizType) {
            case "ORDER_NO":
                return "ORD" + String.format("%010d", value);
            case "REFUND_NO":
                return "REF" + String.format("%08d", value);
            case "BATCH_NO":
                return "BAT" + String.format("%06d", value);
            default:
                return String.valueOf(value);
        }
    }
}
