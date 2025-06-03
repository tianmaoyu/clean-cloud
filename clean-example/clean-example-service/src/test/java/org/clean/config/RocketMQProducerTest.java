package org.clean.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.clean.system.entity.User;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RocketMQProducerTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
   private ObjectMapper  objectMapper;
    @Test
    void sendMessage() throws JsonProcessingException {

        User user = new User();
        user.setName("eric");
        user.setAge(18);
        user.setEmail("eric@test.com");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setCreateId(1L);
        user.setUpdateId(1L);
        user.setUserType(UserType.ADMIN);
        user.setSex(SexEnum.MALE);
        user.setId(1L);

        rocketMQTemplate.convertAndSend("topic-test-user", user);
    }
}