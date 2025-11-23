package org.clean.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import org.clean.example.enums.CodeRuleType;

import java.time.OffsetDateTime;

/**
 * @TableName code_rule_config 编号生成规则
 */
@Data
@TableName("code_rule_config")
public class CodeRuleConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private CodeRuleType bizType;
    
    private Long currentValue;
    
    private Integer stepSize ;
    
    private Integer minCacheSize ;
    
    private String description;
    
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;

}