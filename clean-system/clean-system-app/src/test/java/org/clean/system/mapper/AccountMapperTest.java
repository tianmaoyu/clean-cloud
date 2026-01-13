package org.clean.system.mapper;

import org.clean.Author;
import org.clean.system.entity.Account;
import org.clean.system.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    @Author("zhangsan")
    @Test
    void updateAge() {
        Account account = accountMapper.selectById(1);
        Boolean b = accountMapper.updateAge(9, account.getId());
        assertTrue(b);
    }
    @Author("zhangsan")
    @Test
    void insert() {
        Account account = new Account();
        account.setUserName("test");
        account.setAge(9);
        account.setAccountStatus(AccountStatus.ENABLED);
        account.setBirthday(new Date());
        int insert = accountMapper.insert(account);
    }
}