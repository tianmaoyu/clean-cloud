package org.clean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.clean.example.entity.CodeRuleConfig;
import org.clean.example.enums.CodeRuleType;
import org.clean.mapper.CodeRuleConfigMapper;
import org.clean.service.CodeRuleConfigService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
//import org.springframework.util.CollectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CodeRuleConfigServiceImpl implements CodeRuleConfigService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private CodeRuleConfigMapper codeRuleConfigMapper;

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
    public String getCode(String bizType) {

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
     * 批量获取
     * @param bizType
     * @param count 最多1000
     * @return
     */
    public List<String> getCode(String bizType, int count) {

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
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断 cacheKey:{}",cacheKey, ex);
            throw new RuntimeException(ex);
        }
        catch (Exception ex) {
            log.error("补充缓存编号bizType错误:{}",cacheKey, ex);
            throw ex;
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

    /**
     * 查询 数据库生成 code
     * @param bizType
     * @param count 获取个数
     * @return
     */
    private List<String> buildNo(String bizType, int count) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setTimeout(5);
        TransactionStatus status = transactionManager.getTransaction(def);

        try{

            CodeRuleConfig ruleConfig = codeRuleConfigMapper.selectByBizTypeForLock(CodeRuleType.ORDER);
            log.info("查询结果: {}", ruleConfig.getVersion());
            Assert.notNull(ruleConfig, "规则不存在");

            Long  curentValue = ruleConfig.getCurrentValue();
            Integer step = ruleConfig.getStepSize();

            List<String> codeList = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                curentValue = curentValue + step;
                String code= bulidCode(bizType, curentValue);
                codeList.add(code);
            }

            //更新记录
            ruleConfig.setVersion(ruleConfig.getVersion() + 1);
            ruleConfig.setCurrentValue(curentValue);
            codeRuleConfigMapper.updateById(ruleConfig);

            log.info("更新: {}", ruleConfig);

            transactionManager.commit(status);

            return codeList;

        } catch (Exception ex) {
            transactionManager.rollback(status);
            log.error("生成编号异常 bizType: {} ",bizType, ex);
            throw ex;
        }

    }

    /**
     * 各种规则
     */
    private String bulidCode(String bizType, long value) {
        return bizType+value;
    }
}
