package org.clean.example.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clean.example.enums.CodeRuleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


@Slf4j
@SpringBootTest
class CodeRuleConfigServiceImplTest {

    @Autowired
    private CodeRuleConfigServiceImpl codeRuleConfigService;

    @Test
    void getCode() {
        long startTime = System.currentTimeMillis();

        String result = codeRuleConfigService.getCode(CodeRuleType.ORDER.getCode());
        String result1 = codeRuleConfigService.getCode(CodeRuleType.ORDER.getCode());
        String result2 = codeRuleConfigService.getCode(CodeRuleType.ORDER.getCode());
        String result3 = codeRuleConfigService.getCode(CodeRuleType.ORDER.getCode());
        String result4 = codeRuleConfigService.getCode(CodeRuleType.ORDER.getCode());

        long endTime = System.currentTimeMillis();
        log.info("运行时间: {}ms", endTime - startTime);

        log.info("查询结果: {}", result);
        
    }

    @Test
    void testGetCode() {

        String bizType = CodeRuleType.ORDER.getCode();
        //记录运行时间
        long startTime = System.currentTimeMillis();
        // 执行测试
        List<String> result1 = codeRuleConfigService.getCode(bizType, 1000);
        List<String> result2 = codeRuleConfigService.getCode(bizType, 1000);
        List<String> result3 = codeRuleConfigService.getCode(bizType, 1000);
        List<String> result4 = codeRuleConfigService.getCode(bizType, 1000);
        List<String> result5 = codeRuleConfigService.getCode(bizType, 1000);

        long endTime = System.currentTimeMillis();
        log.info("运行时间: {}ms", endTime - startTime);
        log.info("查询结果: {}", JSON.toJSONString(result5));
    }

    @SneakyThrows
    @Test
    void testGetCodeMulThread() {
        //记录运行时间
        long startTime = System.currentTimeMillis();
        String bizType = CodeRuleType.ORDER.getCode();
        // 线程池并发获取
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();

        ArrayList<Future<List<String>>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Future<List<String>> future1 = executor.submit(() -> codeRuleConfigService.getCode(bizType, 888));
            futures.add(future1);
        }

        List<String> list = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            List<String> codeList = future.get();
            list.addAll(codeList);
        }

        long endTime = System.currentTimeMillis();
        log.info("运行时间: {}ms", endTime - startTime);

        log.info("预期个数: {}",20*888);
        log.info("查询结果: {}", list.size());
        executor.shutdown();
    }

    @Test
    void refillCache() {
        // 准备测试数据
        String bizType = CodeRuleType.ORDER.getCode();

        //记录运行时间
        long startTime = System.currentTimeMillis();
        Boolean b = codeRuleConfigService.refillCache(bizType);

        log.info("结果: {}", b);
    }

    @Test
    void dailyReset() {
        String bizType = CodeRuleType.ORDER.getCode();
        // 执行测试
        Boolean result = codeRuleConfigService.dailyReset(bizType);
       log.info("结果: {}", result);
        
    }

    @SneakyThrows
    @Test
    void configChangeReset() {
        String bizType = CodeRuleType.ORDER.getCode();
        // 执行测试
        Boolean result = codeRuleConfigService.configChangeReset(bizType);
        
       log.info("结果: {}", result);
    }
}