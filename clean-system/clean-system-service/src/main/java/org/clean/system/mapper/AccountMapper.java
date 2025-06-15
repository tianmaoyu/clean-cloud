package org.clean.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.clean.system.entity.Account;
import org.clean.system.entity.User;
import org.clean.system.enums.AccountStatus;

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

    default Boolean updateAge(Integer age, Integer id){
//        LambdaQueryWrapper<Account> lambdaWrapper = Wrappers.lambdaQuery();
//        List<Account> users = new LambdaQueryChainWrapper<>(this)
//                .like(Account::getUserName, "张")
//                .gt(Account::getAge, 18)
//                .orderByDesc(Account::getBirthday)
//                .list();

        return lambdaUpdate()
                .eq(Account::getId, id)
                .set(Account::getAge, age)
                .set(Account::getAccountStatus, AccountStatus.ENABLED)
                .update(new Account());
    }

    List<Account> selectAll();
    Account selectById(Integer id);
    int insert(Account account);
    //不能和上面重复
//    int update(Account account);
    int delete(Integer id);

    default Account getByName(String name){
         Account one = lambdaQuery().eq(Account::getUserName, name).one();
         return one;
    }
}




