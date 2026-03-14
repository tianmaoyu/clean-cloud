package org.clean.system.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RocketMQProducerPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultMQProducer) {
            DefaultMQProducer producer = (DefaultMQProducer) bean;
            DefaultMQProducerImpl defaultMQProducerImpl = producer.getDefaultMQProducerImpl();
            if (defaultMQProducerImpl != null) {
                defaultMQProducerImpl.registerSendMessageHook(new CustomSendMessageHook());
            }
        }
        return bean;
    }
}