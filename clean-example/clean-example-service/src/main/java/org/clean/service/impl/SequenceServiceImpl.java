package org.clean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.clean.example.entity.SequenceGenerator;
import org.clean.service.SequenceService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
    public List<String> getNextSequences(String bizType, int count) {

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
     * 补充缓存编号
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
                //可能上一个获取锁的已经进行填充,并且少量消费,避免重复填充
                if(fillCount<200) return true;

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


    public Boolean clearCache(String bizType){

        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        //那些是否要清除
        Boolean delete = redisTemplate.delete(cacheKey);

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

        // 生成编号
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
