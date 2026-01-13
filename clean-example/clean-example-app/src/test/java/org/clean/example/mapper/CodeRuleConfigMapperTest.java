package org.clean.example.mapper;

import lombok.extern.slf4j.Slf4j;
import org.clean.example.entity.CodeRuleConfig;
import org.clean.example.enums.CodeRuleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class CodeRuleConfigMapperTest {

    @Autowired
    private CodeRuleConfigMapper codeRuleConfigMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void insert() {
        // 先插入一条测试数据
        CodeRuleConfig testConfig = new CodeRuleConfig();
        testConfig.setBizType(CodeRuleType.Other);
        testConfig.setCurrentValue(1L);
        testConfig.setStepSize(1);
        testConfig.setMinCacheSize(100);
        testConfig.setDescription("订单编号生成规则");
        testConfig.setVersion(1L);
        int insert = codeRuleConfigMapper.insert(testConfig);

        log.info("插入结果: {}", testConfig);
    }


    @Test
    void selectByBizType() {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setTimeout(2);

        TransactionStatus status = transactionManager.getTransaction(def);

        try{
            CodeRuleConfig codeRuleConfig = codeRuleConfigMapper.selectByBizTypeForLock(CodeRuleType.ORDER);
            log.info("查询结果: {}", codeRuleConfig.getVersion());

            Long newVersion = codeRuleConfig.getVersion() + 1;
            Long newCurrentValue = codeRuleConfig.getCurrentValue() + codeRuleConfig.getStepSize()* 5;
            codeRuleConfig.setVersion(newVersion);
            codeRuleConfig.setCurrentValue(newCurrentValue);

            codeRuleConfigMapper.updateById(codeRuleConfig);
            log.info("更新: {}", codeRuleConfig);

            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }

    }

    @Transactional
    @Test
    void updateByBizTypeReturn() {
        // 执行更新测试
        CodeRuleConfig updatedConfig = codeRuleConfigMapper.updateByBizTypeReturn(CodeRuleType.USER, 5);
        log.info("更新结果: {}", updatedConfig);
        assertNotNull(updatedConfig);
    }

    @Transactional
    @Test
    void insertBatch() {
        CodeRuleConfig codeRuleConfig = new CodeRuleConfig();
        codeRuleConfig.setBizType(CodeRuleType.Other);
        codeRuleConfig.setCurrentValue(1L);
        codeRuleConfig.setStepSize(1);
        codeRuleConfig.setMinCacheSize(100);
        codeRuleConfig.setDescription("支付流水编号生成规则");
        codeRuleConfig.setVersion(1L);

        CodeRuleConfig codeRuleConfig2 = new CodeRuleConfig();
        codeRuleConfig2.setBizType(CodeRuleType.Other1);
        codeRuleConfig2.setCurrentValue(1L);
        codeRuleConfig2.setStepSize(1);
        codeRuleConfig2.setMinCacheSize(100);
        codeRuleConfig2.setDescription("支付流水编号生成规则");
        codeRuleConfig2.setVersion(1L);
        
        boolean insertResult = codeRuleConfigMapper.insertBatch(
        new ArrayList<CodeRuleConfig>() {{
            add(codeRuleConfig);
            add(codeRuleConfig2);
        }});
        
        assertTrue(insertResult);
        log.info("批量插入结果: {}", insertResult);
    }
}