package org.clean.system.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;

import java.util.Date;

@ApiModel
@Data
public class UserDto {
    private Long id;
    private String name;
    private Integer age;
    private String email;

    private UserType userType;
    private SexEnum sex;

    @TableField(fill = FieldFill.INSERT)
    private Long createId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
