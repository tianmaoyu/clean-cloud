package org.clean.config;

import org.clean.system.entity.User;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RocketMQProducerTest {

    @Autowired
    private RocketMQProducer rocketMQProducer;
    @Test
    void sendMessage() {

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

        rocketMQProducer.sendMessage("topic-test-user", user);
    }
}