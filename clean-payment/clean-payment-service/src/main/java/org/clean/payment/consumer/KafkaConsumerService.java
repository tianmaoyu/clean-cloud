package org.clean.payment.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "test-topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
       log.info("接收到消息:key:{} value:{} ",record.key(),record.value());

        Boolean success = doSomething(record.value());

        if( success) {
            ack.acknowledge();
        }else{
            //手动重试
//            ack.nack(1000);
            log.error("消费失败，等待重试");
            throw new RuntimeException("消费失败，等待重试");
        }
    }

    private Boolean doSomething(String str){
        int i = Integer.parseInt(str);
        return i > 0;
    }
}