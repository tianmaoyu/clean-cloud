package org.clean.system.consumer;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
    topic = "test-topic",
    consumerGroup = "my-consumer-group",
    selectorType = SelectorType.TAG,           // 消息过滤类型，TAG或SQL92
    selectorExpression = "tag1 || tag2",        // 过滤表达式
    consumeMode = ConsumeMode.CONCURRENTLY,     // 消费模式：并发或顺序
    messageModel = MessageModel.CLUSTERING,     // 消息模型：集群或广播
    consumeThreadMax = 64,                       // 最大消费线程数
    consumeTimeout = 30000,                      // 消费超时时间，单位毫秒
    replyTimeout = 30000,                        // 回复超时时间
    enableMsgTrace = true,                         // 开启消息轨迹
    customizedTraceTopic = "my-trace-topic"        // 自定义轨迹Topic
)
public class MyConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        // 消费逻辑
    }
}