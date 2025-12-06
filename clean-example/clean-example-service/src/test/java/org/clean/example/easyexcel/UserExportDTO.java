package org.clean.example.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.clean.example.config.GenericIntegerIEnumConverter;
import org.clean.example.config.GenericStringIEnumConverter;
import org.clean.example.enums.UserStatus;

@Data
public class UserExportDTO {
    
    @ExcelProperty("用户ID")
    private Long id;
    
    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty(value = "状态", converter = GenericStringIEnumConverter.class)
    private UserStatus status;
    
    @ExcelProperty(value = "角色", converter = GenericIntegerIEnumConverter.class)
    private UserRole role;
    
    @ExcelProperty("邮箱")
    private String email;
}