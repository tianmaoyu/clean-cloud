package org.clean.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.clean.system.entity.Account;
import org.clean.system.entity.TransactionMessage;

public interface TransactionMessageMapper extends BaseMapper<TransactionMessage> {
    default LambdaUpdateChainWrapper<TransactionMessage> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(this);
    }
    default LambdaQueryChainWrapper<TransactionMessage> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(this);
    }

}
