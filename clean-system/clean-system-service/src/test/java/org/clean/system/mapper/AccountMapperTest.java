package org.clean.system.mapper;

import org.clean.system.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    @Test
    void updateAge() {
        Account account = accountMapper.selectById(1);
        Boolean b = accountMapper.updateAge(10, account.getId());
        assertTrue(b);
    }
}