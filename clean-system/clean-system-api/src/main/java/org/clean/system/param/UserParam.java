package org.clean.system.param;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.ToString;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;

import java.util.Date;

@ToString
@Data
public class UserParam {
    private Long id;
    private String name;
    private Integer age;
    private String email;

    private UserType userType;
    private SexEnum sex;

    private Long createId;

    private Date createTime;

    private Long updateId;

    private String updateUserName;

    private String updateTime;
}
