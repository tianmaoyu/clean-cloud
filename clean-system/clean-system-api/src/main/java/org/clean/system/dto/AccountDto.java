package org.clean.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.clean.system.enums.AccountStatus;

import java.time.LocalDateTime;

/**
 * @TableName account
 */
@ApiModel
@Data
public class AccountDto {
    private Integer id;
    private String userName;
    private Integer age;
    private LocalDateTime birthday;
    private AccountStatus accountStatus;
}