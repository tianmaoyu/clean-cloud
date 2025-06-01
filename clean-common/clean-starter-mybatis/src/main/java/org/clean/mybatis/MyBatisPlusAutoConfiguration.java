package org.clean.mybatis;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.apache.ibatis.exceptions.IbatisException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.clean.tenum.IEnum;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Set;

@Configuration
public class MyBatisPlusAutoConfiguration {

    @Bean
    public ConfigurationCustomizer mybatisTypeHandlerRegistryCustomizer() {
        return configuration -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();

            Set<Class<? extends IEnum>> set = new Reflections("org.clean").getSubTypesOf(IEnum.class);
            // 扫描所有实现 IEnum 的枚举
            set.stream()
                .filter(Class::isEnum)
                .forEach(enumClass -> {
                    // 解析 code 类型
                    Class<?> codeType = resolveCodeType(enumClass);
                    // 动态注册类型处理器
                    registry.register(enumClass, resolveJdbcType(codeType),new EnumTypeHandler<>((Class) enumClass));
                });
        };
    }

    // 解析枚举的 code 泛型类型
    public Class<?> resolveCodeType(Class<?> enumClass) {
        return Arrays.stream(enumClass.getGenericInterfaces())
            .filter(t -> t instanceof ParameterizedType)
            .map(t -> (ParameterizedType) t)
            .filter(t -> t.getRawType().equals(IEnum.class))
            .findFirst()
            .map(t -> (Class<?>) t.getActualTypeArguments()[0])
            .orElseThrow(() -> new IbatisException(
                "无法解析枚举 " + enumClass.getName() + " 的 code 类型"));
    }

    // 根据 code 类型推断 JDBC 类型
    public JdbcType resolveJdbcType(Class<?> codeType) {
        if (Integer.class.isAssignableFrom(codeType)) {
            return JdbcType.INTEGER;
        } else if (String.class.isAssignableFrom(codeType)) {
            return JdbcType.VARCHAR;
        } else if (Long.class.isAssignableFrom(codeType)) {
            return JdbcType.BIGINT;
        }
        return JdbcType.OTHER;
    }
}