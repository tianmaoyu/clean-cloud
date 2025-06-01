package org.clean.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.clean.system.entity.User;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "topic-test-user", consumerGroup = "my-consumer-group")
public class MyConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String user) {
        System.out.println("收到消息：" + user);
    }
}