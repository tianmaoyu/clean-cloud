package org.clean.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.clean.system.entity.Account;
import org.clean.system.entity.User;

import java.util.List;

/**
* @author eric
* @description 针对表【account】的数据库操作Mapper
* @createDate 2025-05-24 21:14:55
* @Entity com.example.demo.domain.Account
*/
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    default LambdaUpdateChainWrapper<Account> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(this);
    }

    default LambdaQueryChainWrapper<Account> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(this);
    }

    List<Account> selectAll();
    Account selectById(Integer id);
    int insert(Account account);
    int update(Account account);
    int delete(Integer id);

    default Account getByName(String name){
         Account one = lambdaQuery().eq(Account::getUserName, name).one();
         return one;
    }
}




