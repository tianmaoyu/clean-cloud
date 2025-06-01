package org.clean.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.clean.system.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @TableName account
 */
@Data
public class Account {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;
    private String userName;
    private Integer age;
    private Date birthday;
    private AccountStatus accountStatus;
}