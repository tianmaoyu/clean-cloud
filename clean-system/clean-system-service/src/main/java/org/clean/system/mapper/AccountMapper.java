package org.clean.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.clean.system.entity.Account;

import java.util.List;

/**
* @author eric
* @description 针对表【account】的数据库操作Mapper
* @createDate 2025-05-24 21:14:55
* @Entity com.example.demo.domain.Account
*/
@Mapper
public interface AccountMapper {
    List<Account> selectAll();
    Account selectById(Integer id);
    int insert(Account account);
    int update(Account account);
    int delete(Integer id);

}




