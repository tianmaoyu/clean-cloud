package org.clean.consumer;

import jodd.io.findfile.ClassScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Slf4j
@Component
public class UserConsumer{







//    @Resource
//    private OrderMessageMetrics metrics;
//    @Autowired
//    private MQPushConsumer consumer;


//    RocketMQListener



//    @Component
//    @RocketMQMessageListener(topic = "user-topic1",consumerGroup = "user-group")
//    public class UserConsumer1 implements MessageListenerConcurrently, RocketMQListener<String> {
//        @Override
//        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//            for (MessageExt messageExt : msgs) {
//                try {
//
//                  log.info("info:{}", messageExt.getBody());
//
//                } catch (Exception e) {
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//            }
//
//            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//        }
//
//        @Override
//        public void onMessage(String message) {
//            log.info("info:{}", message);
//        }
//    }



//
//    @Component
//    @RocketMQMessageListener(topic = "user-topic2",consumerGroup = "user-group")
//    public class UserConsumer2 implements MessageListenerConcurrently {
//        @Override
//        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//            for (MessageExt messageExt : msgs) {
//                try {
//                    long now = System.currentTimeMillis();
//
//
//                } catch (Exception e) {
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//            }
//
//            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//        }
//
//    }

}
