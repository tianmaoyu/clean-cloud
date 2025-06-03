package org.clean.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.clean.system.entity.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
@RocketMQMessageListener(
        topic = "topic-test-user",
        consumerGroup = "my-consumer-group"
)
public class UserConsumer implements RocketMQListener<User> {
    @Override
    public void onMessage(User user) {
       System.out.println("收到消息：" + user);
    }
}
