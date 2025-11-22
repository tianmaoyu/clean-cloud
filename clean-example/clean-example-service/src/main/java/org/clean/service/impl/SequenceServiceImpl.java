package org.clean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.clean.example.entity.SequenceGenerator;
import org.clean.service.SequenceService;
import org.ehcache.core.util.CollectionUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
//import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.apache.commons.collections4.CollectionUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final int DEFAULT_FILL_COUNT = 5000;
    // 填充阈值（当缓存数量小于此值时触发填充）
    private static final int FILL_THRESHOLD = 2500;
    //单次最大取数
    private static final int MAX_BATCH_COUNT = 1000;

    /**
     * 获取一个编号
     */
    public String getNextSequence(String bizType) {

        Assert.hasText(bizType, "bizType can not be empty");

        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        // 一次缓存获取
        String sequence = this.cacheRightPop(cacheKey);
        if (sequence != null)  return sequence;
        log.warn(" 一次缓存获取失败. bizType: {}", bizType);

        //填充缓存
        this.refillCache(bizType);

        //二次缓存获取
        sequence = this.cacheRightPop(cacheKey);
        if(sequence != null)  return sequence;
        log.warn(" 二次缓存获取失败. bizType: {}", bizType);

        //三次数据库保底
        sequence = this.buildNo(bizType);
        if(sequence != null) return sequence;
        log.error("[单次]生产编号失败. bizType: {}", bizType);
        throw new RuntimeException("[单次]生产编号失败. bizType: " + bizType);
    }

    /**
     * 批量获取编号
     */
    public List<String> getNextSequence(String bizType, int count) {

        Assert.isTrue(count > 0, "count must be greater than 0");
        Assert.isTrue(count <= MAX_BATCH_COUNT, "count must be less than or equal to 1000");
        Assert.hasText(bizType, "bizType can not be empty");

        List<String> result=new ArrayList<>();
        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;

        //一次缓存获取
        List<String> _list1 = this.cacheRightPop(cacheKey, count);
        if(CollectionUtils.isNotEmpty(_list1)) result.addAll(_list1);
        if(result.size()==count) return result;
        log.warn("[批量]一次缓存获取失败. bizType: {} cacheCount:{}", bizType,result.size());

        //填充缓存
        this.refillCache(bizType);

        //二次缓存获取
        int needCount = count-result.size();
        List<String> _list2 = this.cacheRightPop(cacheKey, needCount);
        if(CollectionUtils.isNotEmpty(_list2) ) result.addAll(_list2);
        if(result.size() == count) return result;
        log.warn("[批量]二次缓存获取失败. bizType: {} 获得count:{}", bizType,result.size());

        //三次数据库保底
        needCount = count-result.size();
        List<String> _list3 = this.buildNo(bizType, needCount);
        if(CollectionUtils.isNotEmpty(_list3) ) result.addAll(_list3);
        if(result.size() == count)  return result;
        log.error("[批量]生产编号失败. bizType: {} 获得count:{}", bizType,result.size());
        throw new RuntimeException("[批量]生产编号失败. bizType: " + bizType);
    }

    private List<String> cacheRightPop(String cacheKey, int count) {
        try {
            return redisTemplate.opsForList().rightPop(cacheKey, count);
        } catch (Exception e) {
            log.error("缓存获取异常. cacheKey: {}", cacheKey, e);
        }
        return new ArrayList<>();
    }
    private String cacheRightPop(String cacheKey) {
        try {
            return redisTemplate.opsForList().rightPop(cacheKey);
        } catch (Exception e) {
            log.error("缓存获取异常. cacheKey: {}", cacheKey, e);
        }
        return null;
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

                Long cacheSize = redisTemplate.opsForList().size(cacheKey);
                cacheSize=cacheSize==null?0:cacheSize;
                //缓存数量大于阈值则不进行填充
                if(cacheSize > FILL_THRESHOLD) return true;

                int fillCount = DEFAULT_FILL_COUNT- cacheSize.intValue();
                List<String> noList = this.buildNo(bizType, fillCount);

                redisTemplate.opsForList().leftPushAll(cacheKey, noList);

                log.info("补充编号bizType: {}, 剩余数量: {}, 新填充数量: {}",bizType,cacheSize,fillCount);

                return true;
            }
        }  catch (Exception e) {
            log.error("补充缓存编号bizType错误:{}",cacheKey, e);
            throw new RuntimeException(e);
        }finally {
            // 当前线程获得锁,则释放锁
            if (lock.isHeldByCurrentThread()) lock.unlock();

        }
        return false;
    }


    /**
     * 每日重置 - 并发安全
     * @param bizType
     * @return
     */
    public Boolean dailyReset(String bizType) {
        try {
            // 1. 清理缓存
            boolean clearResult = dailyClearCache(bizType);
            if (!clearResult) {
                log.error("清理缓存失败. bizType: {}", bizType);
                return false;
            }

            // 2. 填充缓存（需要加锁，因为涉及数据库操作）
            boolean fillResult = refillCache(bizType);
            if (!fillResult) {
                log.error("填充缓存失败. bizType: {}", bizType);
                return false;
            }

            log.info("每日重置成功. bizType: {}", bizType);
            return true;
        } catch (Exception e) {
            log.error("每日重置执行异常. bizType: {}", bizType, e);
            return false;
        }
    }

    /**
     * 每日凌晨清理重新设置 - 并发安全- 每天最多执行一次
     * @param bizType
     * @return
     */
    private Boolean dailyClearCache(String bizType){

        // 使用日期作为标记，确保每天只清理一次
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String markerKey = sequence_clean_key_prefix + today+ ":" +bizType ;
        // 使用 setIfAbsent 原子操作设置标记
        Boolean setSuccess = redisTemplate.opsForValue().setIfAbsent(markerKey, "1", 25, TimeUnit.HOURS);
        if (!Boolean.TRUE.equals(setSuccess))  return true;

        String cacheKey = SEQUENCE_CACHE_KEY_PREFIX + bizType;
        redisTemplate.delete(cacheKey);
        log.info("清理缓存完成bizType: {}", bizType);
        return true;
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

    private String buildNo(String bizType){
        List<String> strings = buildNo(bizType, 1);
        return strings.get(0);
    }
//    更新并返回旧值 - 使用 RETURNING 子句
//    @Select("UPDATE sequence_generator SET " +
//            "current_value = current_value + #{step}, " +
//            "version = version + 1, " +
//            "updated_time = NOW() " +
//            "WHERE biz_type = #{bizType} " +
//            "RETURNING id, biz_type, current_value - #{step} as old_current_value, " +
//            "current_value as new_current_value, step_size, min_cache_size, " +
//            "description, version - 1 as old_version, version as new_version, " +
//            "created_time, updated_time")
    private List<String> buildNo(String bizType, int generateCount) {

        //独立的事务
        //直接更新数据库 并且 returning *;

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
        // 乐观锁-更新数据库 CurrentValue 类似于 version 的版版本号来使用
        generator.setCurrentValue(startValue + stepSize);
        // 更新的使用 使用 乐观锁把
//        int updatedRows = sequenceGeneratorMapper.updateWithOptimisticLock(generator);
//
//        if (updatedRows == 0) {
//            // 乐观锁冲突，抛出异常触发重试
//            throw new OptimisticLockException("序列号生成乐观锁冲突");
//        }

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
