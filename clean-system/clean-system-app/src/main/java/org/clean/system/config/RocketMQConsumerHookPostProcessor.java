package org.clean.system.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQConsumerHookPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultRocketMQListenerContainer) {
            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) bean;
            DefaultMQPushConsumer consumer = container.getConsumer();
            if (consumer != null && consumer.getDefaultMQPushConsumerImpl() != null) {
                consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(new CustomConsumeMessageHook());
            }
        }
        return bean;
    }
}