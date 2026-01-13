package org.clean.system.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.clean.system.entity.TransactionMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 事件监听器
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageEventListener {
    
//    private final RocketMQTemplate rocketMQTemplate;

    // 事务要到单独的服务中注册,不要在服务的公共包注册
    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        classes = TransactionMessage.class
    )
    public void messageSavedEvent(TransactionMessage msg) {

        try {

//            rocketMQTemplate.convertAndSend(msg.getMqTopic(), msg.getContent());
            //发送成功
            log.info("消息发送成功，消息ID: {}", msg);
            
        } catch (Exception e) {
            log.error("消息发送失败，消息ID: {}", msg, e);
            // 更新为失败状态，后续可通过补偿机制重试

        }
    }
}