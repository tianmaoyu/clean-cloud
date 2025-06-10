package org.clean.system.service.impl;

import org.clean.system.entity.Account;
import org.clean.system.mapper.AccountMapper;
import org.clean.system.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author eric
* @description 针对表【account】的数据库操作Service实现
* @createDate 2025-05-24 21:14:55
*/
@Service
public class AccountServiceImpl  implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public Account selectById(Integer id) {
        return accountMapper.selectById(id);
    }
    @Override
    public List<Account> selectAll() {
        return accountMapper.selectAll();
    }
    @Override
    public int insert(Account account) {
        return accountMapper.insert(account);
    }
    @Override
    public int update(Account account) {
        boolean b = accountMapper.insertOrUpdate(account);
        return 1;
    }
    @Override
    public int delete(Integer id) {
        return accountMapper.delete(id);
    }

    @Override
    public Account getByName(String name) {
      return   accountMapper.getByName(name);
    }
}




