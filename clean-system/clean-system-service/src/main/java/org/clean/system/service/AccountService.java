package org.clean.system.service;


import org.clean.system.entity.Account;

import java.util.List;

/**
 * @author eric
 * @description 针对表【account】的数据库操作Service
 * @createDate 2025-05-24 21:14:55
 */
public interface AccountService {

    Account selectById(Integer id);

    List<Account> selectAll();

    int insert(Account account);

    int update(Account account);

    int delete(Integer id);

    Account getByName(String name);
}
