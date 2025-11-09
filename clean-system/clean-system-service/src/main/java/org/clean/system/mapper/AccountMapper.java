package org.clean.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.MapperProxyMetadata;
import com.baomidou.mybatisplus.core.toolkit.MybatisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.clean.mybatis.BatchUtils;
import org.clean.system.entity.Account;
import org.clean.system.entity.User;
import org.clean.system.enums.AccountStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

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

    default Boolean insertBatch(List<Account> accountList){
        Boolean success = BatchUtils.insertBatch(this, accountList);
        return success;
    }

    /**
     * 批量插入 todo 待验证
     * @param accountList
     * @return
     */
    default Boolean insertBatch(ArrayList<Account> accountList){
        SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(Account.class);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            int i = 0;
            for (Account entity : accountList) {
                // 只是添加到批处理缓存
                this.insert(entity);
                if (i >= 1 && i % 200 == 0) {
                    //真正发送数据库执行
                    sqlSession.flushStatements();
                }
                i++;
            }
            sqlSession.flushStatements();
            return true;
        }

    }
}




