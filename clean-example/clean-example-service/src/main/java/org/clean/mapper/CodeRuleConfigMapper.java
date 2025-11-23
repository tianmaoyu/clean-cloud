package org.clean.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.clean.example.entity.CodeRuleConfig;
import org.clean.example.enums.CodeRuleType;

import java.util.ArrayList;

/**
* @author eric
* @description 针对表【code_rule_config】的数据库操作Mapper
* @createDate 2025-05-24 21:14:55
*/
@Mapper
public interface CodeRuleConfigMapper extends BaseMapper<CodeRuleConfig> {
    default LambdaUpdateChainWrapper<CodeRuleConfig> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(this);
    }

    default LambdaQueryChainWrapper<CodeRuleConfig> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(this);
    }

    /**
     * 加锁 - 查询
     * @param bizType 业务类型
     * @return
     */
    CodeRuleConfig selectByBizTypeForUpdate(CodeRuleType bizType);

    CodeRuleConfig updateByBizTypeReturn(@Param("bizType") CodeRuleType bizType, @Param("count") int count);

    default Boolean insertBatch(ArrayList<CodeRuleConfig> accountList){
       return Db.saveBatch(accountList);
    }
}