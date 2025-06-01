package org.clean.system.entity;

import lombok.Data;
import org.clean.system.enums.AccountStatus;

import java.time.LocalDateTime;

/**
 * @TableName account
 */
@Data
public class Account {
    private Integer id;
    private String userName;
    private Integer age;
    private LocalDateTime birthday;
    private AccountStatus accountStatus;
}