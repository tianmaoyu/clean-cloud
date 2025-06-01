package org.clean.tenum;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

public  class TEnumConverter implements ConditionalGenericConverter {

        // 支持所有数字类型和字符串类型转换为枚举
        private static final Set<ConvertiblePair> CONVERTIBLE_TYPES = Set.of(
            new ConvertiblePair(String.class, IEnum.class),
            new ConvertiblePair(Number.class, IEnum.class),
            new ConvertiblePair(Integer.class, IEnum.class),
            new ConvertiblePair(Long.class, IEnum.class)
        );

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return CONVERTIBLE_TYPES;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // 确保目标类型是枚举且实现了 IEnum
            return targetType.getType().isEnum() 
                && IEnum.class.isAssignableFrom(targetType.getType());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }

            Class<? extends IEnum<?>> enumType = (Class<? extends IEnum<?>>) targetType.getType();
            return convertToEnum(source, enumType);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private <T extends Enum<T> & IEnum<?>> T convertToEnum(Object code, Class<? extends IEnum<?>> enumType) {
            // 获取枚举的实际 code 类型
            Class<?> codeType = resolveCodeType((Class<T>) enumType);

            // 将输入值转换为 code 的实际类型
            Object convertedCode = convertCodeValue(code, codeType);

            // 查找匹配的枚举实例
            return (T) Arrays.stream(enumType.getEnumConstants())
                .filter(e -> e.getCode().equals(convertedCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "无效枚举值 [" + code + "] for " + enumType.getSimpleName()));
        }

        /**
         * 解析枚举的 code 泛型类型
         */
//        private <T > Class<?> resolveCodeType(Class<T> enumClass) {
//            ParameterizedType genericInterface = (ParameterizedType) enumClass.getGenericInterfaces()[0];
//            Class<? extends Type> aClass = genericInterface.getActualTypeArguments()[0].getClass();
//            return aClass;
//        }

    private <T> Class<?> resolveCodeType(Class<T> enumClass) {
        Type[] interfaces = enumClass.getGenericInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("枚举未实现 IEnum 接口");
        }

        if (!(interfaces[0] instanceof ParameterizedType)) {
            throw new IllegalArgumentException("IEnum 接口未指定泛型参数");
        }

        ParameterizedType parameterizedType = (ParameterizedType) interfaces[0];
        Type[] typeArgs = parameterizedType.getActualTypeArguments();

        if (typeArgs.length == 0) {
            throw new IllegalArgumentException("IEnum 接口缺少泛型参数");
        }

        Type codeType = typeArgs[0];

        // 如果是具体的 Class 类型，直接返回
        if (codeType instanceof Class<?>) {
            return (Class<?>) codeType;
        }

        // 如果是泛型变量（如 T），则抛出异常或默认返回 Object.class
        throw new IllegalArgumentException("无法解析泛型参数: " + codeType.getTypeName());
    }

    /**
         * 将输入值转换为实际的 code 类型
         */
        private Object convertCodeValue(Object code, Class<?> targetType) {
            if (code == null) {
                return null;
            }

            if (targetType.isInstance(code)) {
                return code;
            }

            // 处理字符串到数字类型的转换
            if (code instanceof String) {
                String strValue = (String) code;
                if (targetType == Integer.class) {
                    return Integer.parseInt(strValue);
                } else if (targetType == Long.class) {
                    return Long.parseLong(strValue);
                } else if (targetType == String.class) {
                    return strValue;
                }
            }

            // 处理数字类型之间的转换
            if (code instanceof Number) {
                Number number = (Number) code;
                if (targetType == Integer.class) {
                    return number.intValue();
                } else if (targetType == Long.class) {
                    return number.longValue();
                } else if (targetType == String.class) {
                    return number.toString();
                }
            }

            throw new IllegalArgumentException("不支持的 code 类型转换: " 
                + code.getClass() + " -> " + targetType);
        }
    }
