package org.clean.Scheduler;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DistributedScheduledTask {

    // 每5分钟执行一次
//    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledTask() {

    }

    /**
     * 每天凌晨重置检查（可选）
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void dailyResetCheck() {
        log.info("执行每日编号缓存检查...");
    }
}