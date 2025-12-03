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
 * 通用转换器，用于处理所有实现 IEnum<String> 的枚举
 * 不依赖 fromCode 方法，完全通过反射处理
 */
public class GenericStringIEnumConverter implements Converter<IEnum<String>> {

    // 缓存结构: 枚举类 -> (code值 -> 枚举实例)
    private static final Map<Class<?>, Map<String, IEnum<String>>> ENUM_CACHE = new ConcurrentHashMap<>();

    @Override
    public Class<IEnum<String>> supportJavaTypeKey() {
        return null; // 需在字段注解中指定
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public IEnum<String> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        // 校验字段类型
        if (contentProperty == null || contentProperty.getField() == null) {
            return null;
        }
        
        Class<?> enumClass = contentProperty.getField().getType();
        if (!enumClass.isEnum() || !IEnum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException("字段 [" + contentProperty.getField().getName() + 
                    "] 类型必须是实现 IEnum<String> 的枚举类型");
        }
        
        // 获取单元格值
        String cellValue = cellData.getStringValue();
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return null;
        }
        cellValue = cellValue.trim();

        // 从缓存获取枚举映射，不存在则创建
        return ENUM_CACHE.computeIfAbsent(enumClass, k ->
                Arrays.stream(enumClass.getEnumConstants())
                        .map(e -> (IEnum<String>) e)
                        .collect(Collectors.toMap(
                                IEnum::getCode,
                                Function.identity(),
                                (oldValue, newValue) -> oldValue // 处理重复code时保留第一个
                        ))
        ).get(cellValue);
    }

    @Override
    public WriteCellData<?> convertToExcelData(IEnum<String> value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value != null ? value.getCode() : "");
    }
}