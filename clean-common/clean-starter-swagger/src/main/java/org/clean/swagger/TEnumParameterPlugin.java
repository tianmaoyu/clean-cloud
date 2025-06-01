package org.clean.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.clean.common.tenum.IEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Example;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
// 关键点1：设置更高优先级（数值更小）确保先于默认插件执行
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER - 100) 
public class TEnumParameterPlugin implements ParameterBuilderPlugin {
    private final TypeResolver typeResolver = new TypeResolver();

    @Override
    public void apply(ParameterContext context) {
        Class<?> paramType = context.resolvedMethodParameter().getParameterType().getErasedType();

        if (!paramType.isEnum() || !IEnum.class.isAssignableFrom(paramType)) {
            return;
        }

        // 获取枚举的code类型
        ResolvedType type = resolveCodeType(paramType);
        AllowableListValues allowableValues = createAllowableValues(paramType);
        // 关键点2：完全接管参数配置
        context.parameterBuilder()
                .type(type)
//                .name(paramType.getSimpleName())  参数名称
//                .parameterType(allowableValues.getValueType()) // 强制类型为字符串
                .allowableValues(allowableValues)          // 设置自定义允许值
                .description(buildEnumDescription(paramType))              // 完全覆盖描述
                .defaultValue(getDefaultCode(paramType));
//                .scalarExample(createExample(codeType, allowableValues.getValues()));
    // 设置默认值
    }

    private AllowableListValues createAllowableValues(Class<?> enumType) {
        Map<String, String> codeDescMap = new LinkedHashMap<>();
        collectEnumCodes(enumType, codeDescMap);

        // 获取 getCode() 的返回类型
        ResolvedType resolvedType = resolveCodeType(enumType);
        String typeName = getSwaggerTypeName(resolvedType.getErasedType());

        return new AllowableListValues(codeDescMap.keySet().stream().collect(Collectors.toList()), typeName);
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


    private String buildEnumDescription(Class<?> enumType) {
        Map<String, String> codeDescMap = new LinkedHashMap<>();
        collectEnumCodes(enumType, codeDescMap);
        
        StringBuilder sb = new StringBuilder("自定义枚举参数：");
        codeDescMap.forEach((code, desc) -> 
            sb.append("<br>").append(code).append(" - ").append(desc));
        sb.append("<br><br>");
        return sb.toString();
    }

    private void collectEnumCodes(Class<?> enumType, Map<String, String> codeDescMap) {
        try {
            for (Object enumConstant : enumType.getEnumConstants()) {
                Method getCode = enumType.getMethod("getCode");
                Method getDesc = enumType.getMethod("getDesc");
                String code = String.valueOf(getCode.invoke(enumConstant));
                String desc = (String) getDesc.invoke(enumConstant);
                codeDescMap.put(code, desc);
            }
        } catch (Exception e) {
            throw new RuntimeException("枚举必须实现IEnum接口", e);
        }
    }

    private String getDefaultCode(Class<?> enumType) {
        try {
            Object[] constants = enumType.getEnumConstants();
            if (constants.length == 0) return null;
            Method getCode = enumType.getMethod("getCode");
            return String.valueOf(getCode.invoke(constants[0]));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
    private Example createExample(ResolvedType codeType, List<String> allowableValues) {
        return new Example("Array",allowableValues);
    }
}