package org.clean.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.clean.tenum.IEnum;
import org.springframework.stereotype.Component;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class TEnumModelPropertyBuilderPlugin implements ModelPropertyBuilderPlugin {
    private final TypeResolver typeResolver = new TypeResolver();


    @Override
    public void apply(ModelPropertyContext context) {
        Optional<AnnotatedField> fieldOptional = context.getBeanPropertyDefinition()
                .map(BeanPropertyDefinition::getField);
        
        if (!fieldOptional.isPresent()) return;
        
        Field field = fieldOptional.get().getAnnotated();
        Class<?> fieldType = field.getType();
        
        // 仅处理枚举类型
//        if (!fieldType.isEnum()) return;
        if (!fieldType.isEnum() || !IEnum.class.isAssignableFrom(fieldType)) {
            return;
        }
        
        // 获取枚举实例的 code 和 desc
        Object[] enumConstants = fieldType.getEnumConstants();
        List<Object> codes = new ArrayList<>();
        Map<Object, String> codeDescMap = new HashMap<>();
        
        for (Object enumConstant : enumConstants) {
            try {
                Method getCode = enumConstant.getClass().getMethod("getCode");
                Method getDesc = enumConstant.getClass().getMethod("getDesc");
                Object code = getCode.invoke(enumConstant);
                String desc = (String) getDesc.invoke(enumConstant);
                codes.add(code);
                codeDescMap.put(code, desc);
            } catch (Exception e) {
                throw new RuntimeException("枚举必须包含 getCode() 和 getDesc() 方法", e);
            }
        }
        ResolvedType resolvedType = resolveCodeType(fieldType);
        // 修改 Schema 的允许值和描述
        context.getBuilder()
            .type(resolvedType) // 假设 code 是 Integer 类型
            .example(codes.get(0))// 默认填充的值
            .description(buildDescription(codeDescMap))
                .allowableValues(new AllowableValues() {
                });
    }

    private String buildDescription(Map<Object, String> codeDescMap) {
        StringBuilder sb = new StringBuilder("<strong>枚举值:</strong><strong>描述</strong>");

//        // 保留原始描述（如果有）
//        if (originalDesc != null && !originalDesc.trim().isEmpty()) {
//            descBuilder.append(originalDesc).append("<br>");
//        }
        codeDescMap.forEach((code, desc) ->
                sb.append("<br>").append(code).append(": ").append(desc)
        );
        sb.append("<br>");
        return sb.toString();
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }


    private ResolvedType resolveCodeType(Class<?> enumType) {
        try {
            Method getCode = enumType.getMethod("getCode");
            Class<?> codeClass = getCode.getReturnType();
            return typeResolver.resolve(codeClass);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("枚举必须实现getCode方法", e);
        }
    }
}