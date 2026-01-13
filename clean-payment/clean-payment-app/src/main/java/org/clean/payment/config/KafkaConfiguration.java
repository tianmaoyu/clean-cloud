package org.clean.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfiguration {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> producerProps = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        // 配置重试策略
//          FixedBackOff backOff = new FixedBackOff(3000L, 3); // 3秒间隔，最多重试3次
          FixedBackOff backOff = new FixedBackOff(0l, 0);

//        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(4);
//        backOff.setInitialInterval(1000L);  // 初始间隔秒
//        backOff.setMultiplier(5.0);         // 乘数因子

        // 配置死信队列恢复器
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // 返回错误处理器
        return new DefaultErrorHandler(recoverer, backOff);
    }

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {
//
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        // 使用配置文件中的消费者配置
//        factory.setConsumerFactory(consumerFactory);
//        factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
//        factory.setConcurrency(kafkaProperties.getListener().getConcurrency());
//        // 启用手动提交
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
//        // 设置消息转换器
//        factory.setMessageConverter(new StringJsonMessageConverter());
//        // 设置错误处理器，并明确指定 ErrorHandler 类型
//        factory.setCommonErrorHandler(kafkaErrorHandler);
//        return factory;
//    }
}