package org.clean.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;

import javax.validation.constraints.NotNull;


@Data
@ApiModel("用户添加参数")
public class UserAddParam {

    @ApiModelProperty(value = "用户名", required = true)
    @NotNull
    private String name;

    @ApiModelProperty(value = "年龄", required = true)
    @NotNull
    private Integer age;

    @ApiModelProperty(value = "邮箱", required = false)
    private String email;

    @ApiModelProperty(value = "用户类型", required = true)
    @NotNull
    private UserType userType;

    @ApiModelProperty(value = "性别", required = true)
    @NotNull
    private SexEnum sex;
}
