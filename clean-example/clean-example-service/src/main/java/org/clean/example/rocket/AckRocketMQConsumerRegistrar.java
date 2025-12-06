package org.clean.example.rocket;// src/main/java/com/example/config/MyRocketMQConsumerRegistrar.java

import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AckRocketMQConsumerRegistrar implements ApplicationContextAware, DisposableBean, SmartInitializingSingleton {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    private ApplicationContext applicationContext;

    // 存储已创建的消费者，key: consumerGroup + topic，避免重复创建
    private final Map<String, DefaultMQPushConsumer> consumerMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @SneakyThrows
    @Override
    public void afterSingletonsInstantiated() {
        // 获取所有被 @Component 等管理的 Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            // 明确排除自身
            if (applicationContext.getBean(beanName).getClass() == this.getClass()) {
                continue;
            }
            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = bean.getClass();

            // 扫描类中所有被 @MyRocketMQConsumer 标注的方法
            Map<Method, AckRocketMQConsumer> annotatedMethods = MethodIntrospector.selectMethods(clazz,
                (MethodIntrospector.MetadataLookup<AckRocketMQConsumer>) method ->
                    AnnotatedElementUtils.findMergedAnnotation(method, AckRocketMQConsumer.class));

            for (Map.Entry<Method, AckRocketMQConsumer> entry : annotatedMethods.entrySet()) {
                Method method = entry.getKey();
                AckRocketMQConsumer annotation = entry.getValue();

                String key = annotation.consumerGroup() + "@" + annotation.topic();
                DefaultMQPushConsumer consumer = consumerMap.get(key);

                if (consumer == null) {
                    // 创建新消费者
                    consumer = new DefaultMQPushConsumer(annotation.consumerGroup());
                    consumer.setNamesrvAddr(nameServer);
                    consumer.subscribe(annotation.topic(), annotation.tag());
                    consumer.setConsumeThreadMin(annotation.consumeThreadMin());
                    consumer.setConsumeThreadMax(annotation.consumeThreadMax());

                    // 注册监听器
                    consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                        for (MessageExt msg : msgs) {
                            try {
                                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                                // 反射调用目标方法
                                ReflectionUtils.invokeMethod(method, bean, body);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                            }
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    });

                    try {
                        consumer.start();
                        consumerMap.put(key, consumer);
                        System.out.println("🚀 启动消费者: " + key);
                    } catch (Exception e) {
                        System.err.println("❌ 启动消费者失败: " + key + ", 错误: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // 优雅关闭所有消费者
        for (Map.Entry<String, DefaultMQPushConsumer> entry : consumerMap.entrySet()) {
            System.out.println("🛑 关闭消费者: " + entry.getKey());
            entry.getValue().shutdown();
        }
    }
}
