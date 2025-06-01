package org.clean.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.clean.tenum.IEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Order(Integer.MAX_VALUE)
@Component
public class TEnumResponsePlugin implements OperationBuilderPlugin {
    private final TypeResolver typeResolver = new TypeResolver();

    @Override
    public void apply(OperationContext context) {
        // 1. 获取返回类型
        Class<?> returnType = context.getReturnType().getErasedType();


        if (!returnType.isEnum() || !IEnum.class.isAssignableFrom(returnType)) {
            return;
        }
        // 2. 收集枚举元数据
        List<String> codes = new ArrayList<>();
        Map<String, String> codeDescMap = new LinkedHashMap<>();
        collectEnumMetadata(returnType, codes, codeDescMap);

        ResolvedType resolvedType = resolveCodeType(returnType);
        String typeName = resolvedType.getTypeName();
//        String typeName = getSwaggerTypeName(resolvedType.getErasedType());
//        if(typeName.contains("Integer")){
//            typeName=Integer.class.getSimpleName();
//        }
//

        // 3. 修改响应描述和 Schema
        context.operationBuilder()
                //todo 根据 returnType 类型 的到
            .responseModel(new springfox.documentation.schema.ModelRef(typeName))
            .deprecated(buildResponseDescription("", codeDescMap));
    }

    private void collectEnumMetadata(Class<?> enumType, List<String> codes, Map<String, String> codeDescMap) {
        try {
            for (Object enumConstant : enumType.getEnumConstants()) {
                Method getCode = enumType.getMethod("getCode");
                Method getDesc = enumType.getMethod("getDesc");
                Object code = getCode.invoke(enumConstant);
                String desc = (String) getDesc.invoke(enumConstant);
                codes.add(String.valueOf(code));
                codeDescMap.put(String.valueOf(code), desc);
            }
        } catch (Exception e) {
            throw new RuntimeException("枚举必须包含 getCode() 和 getDesc() 方法", e);
        }
    }

    private String buildResponseDescription(String originalDesc, Map<String, String> codeDescMap) {
        StringBuilder sb = new StringBuilder(originalDesc == null ? "" : originalDesc);
        sb.append("\n返回枚举值:");
        codeDescMap.forEach((code, desc) -> sb.append("\n").append(code).append(": ").append(desc));
        return sb.toString();
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
    private String getSwaggerTypeName(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class || type == int.class) {
            return "integer";
        } else if (type == Long.class || type == long.class) {
            return "long";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type == Double.class || type == double.class) {
            return "number";
        } else if (type == Float.class || type == float.class) {
            return "number";
        }
        return "string"; // 默认兜底
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