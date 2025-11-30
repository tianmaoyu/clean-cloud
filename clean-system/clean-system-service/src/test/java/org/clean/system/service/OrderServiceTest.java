package org.clean.system.service;

import lombok.extern.slf4j.Slf4j;
import org.clean.Author;
import org.clean.system.report.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Author(value = "李四", date = "2024-01-16")
public class OrderServiceTest extends BaseTest {

    @Test
    void testCreateOrder() {
        log.info("创建订单");
        assertTrue(true, "订单创建测试应该通过");
    }
    
    @Test
    void testCancelOrder() {
        log.info("取消订单");
        assertTrue(true, "订单取消测试应该通过");
    }
    
    @Test
    @Disabled("暂时禁用这个测试")
    void testRefundOrder() {
        log.info("订单退款");
        assertTrue(true, "订单退款测试应该通过");
    }
}