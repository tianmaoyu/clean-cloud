package org.clean.payment.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    //不指定默认 博获所有异常??
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            dltTopicSuffix = ".dlt",
            autoCreateTopics = "true",
            include = { Throwable.class }
    )
    @KafkaListener(topics = "test-topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
       log.info("接收到消息:key:{} value:{} ",record.key(),record.value());

        Boolean success = doSomething(record.value());
        if( success) {
            ack.acknowledge();
        }else{
            log.error("消费失败，等待重试");
            throw new RuntimeException("消费失败，等待重试");
        }
    }

    private Boolean doSomething(String str){
        int i = Integer.parseInt(str);
        return i > 0;
    }



    //不指定默认 博获所有异常??
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            dltTopicSuffix = ".dlt",
            autoCreateTopics = "true"
//            include = { Throwable.class }
    )
    @KafkaListener(topics = "mul-partitions", groupId = "my-group")
    public void listenMul(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("partitions--------接收到消息:key:{} value:{} ",record.key(),record.value());
        log.info("partitions--------消息的分区:{}",record.partition());
        log.info("partitions--------消息的offset:{}",record.offset());
        log.info("partitions--------消息的headers:{}",record.topic());
        Headers headers = record.headers();


        Boolean success = doSomething(record.value());
        if( success) {
            ack.acknowledge();
        }else{
            log.error("消费失败，等待重试");
            throw new RuntimeException("消费失败，等待重试");
        }
    }

}