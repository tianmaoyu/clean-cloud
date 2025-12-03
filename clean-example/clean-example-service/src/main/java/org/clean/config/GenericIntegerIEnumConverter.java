package org.clean.config;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.clean.tenum.IEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用转换器，用于处理所有实现 IEnum<Integer> 的枚举
 * 不依赖 fromCode 方法，完全通过反射处理
 */
public class GenericIntegerIEnumConverter implements Converter<IEnum<Integer>> {

    // 缓存结构: 枚举类 -> (code值 -> 枚举实例)
    private static final Map<Class<?>, Map<Integer, IEnum<Integer>>> ENUM_CACHE = new ConcurrentHashMap<>();

    @Override
    public Class<IEnum<Integer>> supportJavaTypeKey() {
        return null; // 需在字段注解中指定
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER; // 支持数字类型
    }

    @Override
    public IEnum<Integer> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        // 校验字段类型
        if (contentProperty == null || contentProperty.getField() == null) {
            return null;
        }
        
        Class<?> enumClass = contentProperty.getField().getType();
        if (!enumClass.isEnum() || !IEnum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException("字段 [" + contentProperty.getField().getName() + 
                    "] 类型必须是实现 IEnum<Integer> 的枚举类型");
        }
        
        // 获取单元格值 - 处理数字和字符串两种情况
        Integer cellValue;
        if (cellData.getType() == CellDataTypeEnum.STRING) {
            // 从字符串解析整数
            String stringValue = cellData.getStringValue();
            if (stringValue == null || stringValue.trim().isEmpty()) {
                return null;
            }
            try {
                cellValue = Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("字段 [" + contentProperty.getField().getName() + 
                        "] 的值 '" + stringValue + "' 无法转换为整数", e);
            }
        } else {
            // 从数字获取整数值
            cellValue = cellData.getNumberValue().intValue();
        }
        
        // 从缓存获取枚举映射，不存在则创建
        Map<Integer, IEnum<Integer>> enumMap = ENUM_CACHE.computeIfAbsent(enumClass, k -> 
                Arrays.stream(enumClass.getEnumConstants())
                        .map(e -> (IEnum<Integer>) e)
                        .collect(Collectors.toMap(
                                IEnum::getCode, 
                                Function.identity(),
                                (oldValue, newValue) -> oldValue // 处理重复code时保留第一个
                        ))
        );
        
        // 查找匹配的枚举
        IEnum<Integer> result = enumMap.get(cellValue);
        if (result == null) {
            throw new IllegalArgumentException("找不到代码为 " + cellValue + " 的枚举: " + enumClass.getName() + 
                    "。可用值: " + enumMap.keySet());
        }
        
        return result;
    }

    @Override
    public WriteCellData<?> convertToExcelData(IEnum<Integer> value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value.getCode().toString());
    }
}