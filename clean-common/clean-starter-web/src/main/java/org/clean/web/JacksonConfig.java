package org.clean.web;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    // 使用常量定义格式模式
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    // 创建线程安全的格式化器实例
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 1. 基础配置
            configureBasicSettings(builder);
            // 2. 日期时间处理
            configureDateTimeHandling(builder);
            // 3. 数字类型处理
            configureNumberHandling(builder);
            // 4. Java 8 时间类型处理
            configureJavaTimeModule(builder);
        };
    }

    private void configureBasicSettings(Jackson2ObjectMapperBuilder builder) {
        builder
            .failOnEmptyBeans(false)          // 空对象不抛异常
            .failOnUnknownProperties(false)    // 改为false: 遇到未知属性不抛异常(更安全)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 禁用时间戳格式
            .timeZone(TimeZone.getTimeZone("Asia/Shanghai")) // 使用更明确的时区ID
            .dateFormat(new SimpleDateFormat(DATETIME_PATTERN)); // 设置默认日期格式
    }

    private void configureDateTimeHandling(Jackson2ObjectMapperBuilder builder) {
        // 统一处理java.util.Date
        builder.simpleDateFormat(DATETIME_PATTERN);
    }

    private void configureNumberHandling(Jackson2ObjectMapperBuilder builder) {
        // 解决Long/BigInteger在前端的精度丢失问题
        builder.serializerByType(Long.class, ToStringSerializer.instance);
        builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
    }

    private void configureJavaTimeModule(Jackson2ObjectMapperBuilder builder) {
        // 创建并配置JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        // 添加序列化器
//        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
//        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
//        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
//
//        // 添加反序列化器
//        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
//        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
//        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
//
        // 注册模块
        builder.modules(javaTimeModule);
    }
}