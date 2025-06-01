package org.clean.swagger;

import org.clean.tenum.IEnum;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER - 100)
@Component
public class TEnumModelBuilderPlugin implements ModelBuilderPlugin {

    @Override
    public void apply(ModelContext context) {
        // 1. 获取模型类型
        Class<?> type = context.getType().getErasedType();


        if (!type.isEnum() || !IEnum.class.isAssignableFrom(type)) {
            return;
        }

        // 2. 准备元数据
        List<Object> codes = new ArrayList<>();
        Map<String, String> codeDescMap = new HashMap<>();
        collectEnumMetadata(type, codes, codeDescMap);

        // 3. 修改 Model 属性
        modifyModelProperties(context, codes, codeDescMap);
    }

    private void collectEnumMetadata(Class<?> enumType, List<Object> codes, Map<String, String> codeDescMap) {
        try {
            for (Object enumConstant : enumType.getEnumConstants()) {
                Method getCode = enumConstant.getClass().getMethod("getCode");
                Method getDesc = enumConstant.getClass().getMethod("getDesc");
                Object code = getCode.invoke(enumConstant);
                String desc = (String) getDesc.invoke(enumConstant);
                codes.add(code);
                codeDescMap.put(code.toString(), desc);
            }
        } catch (Exception e) {
            throw new RuntimeException("枚举必须包含 getCode() 和 getDesc() 方法", e);
        }
    }
    private void modifyModelProperties(ModelContext context, List<Object> codes, Map<String, String> codeDescMap) {
        Model model = context.getBuilder().build();
        Map<String, ModelProperty> properties = model.getProperties();


        for (Map.Entry<String, ModelProperty> entry : properties.entrySet()) {
            ModelProperty property = entry.getValue();


            if (property.getType().getErasedType().isEnum()) {
                // 获取原属性所有参数
                ModelProperty newProperty = new ModelProperty(
                        property.getName(),
                        property.getType(),
                        property.getQualifiedType(),
                        property.getPosition(),
                        property.isRequired(),
                        property.isHidden(),
                        property.isReadOnly(),
                        property.isAllowEmptyValue(), // 新增参数
                        (property.getDescription() == null ? "" : property.getDescription())
                                + " 可选值: " + buildCodeDescText(codeDescMap),
                        new AllowableListValues( codes.stream().map(code -> code.toString()).collect(Collectors.toList()), "String"), // 关键修改：用 code 替换枚举名称
                        property.getExample(),
                        property.getPattern(),       // 新增参数
                        property.getDefaultValue(), // 新增参数
                        property.getXml(),           // 新增参数
                        property.getVendorExtensions()
                );

                properties.put(entry.getKey(), newProperty);
            }
        }
    }

    private String buildCodeDescText(Map<String, String> codeDescMap) {
        StringBuilder sb = new StringBuilder();
        codeDescMap.forEach((code, desc) -> sb.append(code).append(":").append(desc).append(", "));
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}