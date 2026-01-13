package org.clean.example.rocket;// src/main/java/com/example/annotation/MyRocketMQConsumer.java

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AckRocketMQConsumer {
    String topic();
    String consumerGroup() default "DEFAULT_CONSUMER";
    String tag() default "*";
    int consumeThreadMin() default 10;
    int consumeThreadMax() default 20;
}