package org.clean.example.Scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CodeRuleConfigScheduledTask {

    // 每5分钟执行一次
//    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledTask() {

    }

    /**
     * 每天凌晨重置检查（可选）
     */
    @Scheduled(cron = "1 0 0 * * ?")
    public void dailyResetCheck() {
        log.info("执行每日编号缓存检查...");
    }
}