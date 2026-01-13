package org.clean.system.service;

import org.clean.Author;
import org.clean.system.entity.TransactionMessage;
import org.clean.system.enums.MessageStatus;
import org.clean.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Author("admin")
public class TransactionMessageServiceTest extends BaseTest {

    @Autowired
    private TransactionMessageService transactionMessageService;

    @Test
    void save() {
        TransactionMessage message = new TransactionMessage();
        message.setMqTopic("test");
        message.setBusinessId( "1111");
        message.setMqTag("tag");
        message.setContent("content");
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);
        message.setCreatedTime(new Date());
        message.setUpdatedTime(new Date());
        transactionMessageService.save(message);
    }

    @Test
    void save2() {
        TransactionMessage message = new TransactionMessage();
        message.setMqTopic("test");
        message.setBusinessId( "1111");
        message.setMqTag("tag");
        message.setContent("content");
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);
        message.setCreatedTime(new Date());
        message.setUpdatedTime(new Date());
        transactionMessageService.save2(message);
    }

    @Test
    void update() {
    }
}