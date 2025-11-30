package org.clean.system.service;

import org.clean.system.entity.Account;
import org.clean.system.enums.AccountStatus;
import org.clean.system.report.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
public class AccountServiceTest extends BaseTest {

    @Autowired
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(100);
        testAccount.setUserName("test_account");
        testAccount.setAge(16);
        testAccount.setAccountStatus(AccountStatus.ENABLED);
        testAccount.setBirthday(new Date());
    }

    @Test
    void testSelectById() {
        // 插入一个账户
        accountService.insert(testAccount);

        // 获取插入后的账户
        Account savedAccount = accountService.selectById(testAccount.getId());

        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getUserName()).isEqualTo("test_account");
    }

    @Test
    void testSelectAll() {
        // 插入两个账户
        testAccount.setUserName("account_1");
        accountService.insert(testAccount);

        Account anotherAccount = new Account();
        anotherAccount.setUserName("account_2");
        anotherAccount.setAge(10);
        accountService.insert(anotherAccount);

        anotherAccount= accountService.getByName("account_2");
        assertThat(anotherAccount.getUserName()).isEqualTo( "account_2");

        List<Account> accounts = accountService.selectAll();
        assertThat(accounts.size()).isGreaterThan(1);

    }

    @Test
    void testInsert() {
        int rowsAffected = accountService.insert(testAccount);
        assertThat(rowsAffected).isEqualTo(1);

        Account savedAccount = accountService.selectById(testAccount.getId());
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getAge()).isEqualTo(16);
    }

    @Test
    void testUpdate() {
        // 插入初始账户
        accountService.insert(testAccount);

        // 修改账户信息
        testAccount.setUserName("updated_account");
        testAccount.setAge(200);
        int rowsAffected = accountService.update(testAccount);

        assertThat(rowsAffected).isEqualTo(1);

        // 验证更新结果
        Account updatedAccount = accountService.selectById(testAccount.getId());
        assertThat(updatedAccount.getUserName()).isEqualTo("updated_account");
        assertThat(updatedAccount.getAge()).isEqualTo(200);
    }

    @Test
    void testDelete() {
        // 插入账户
        accountService.insert(testAccount);

        // 删除账户
        int rowsAffected = accountService.delete(testAccount.getId());

        assertThat(rowsAffected).isEqualTo(1);

        // 验证删除结果
        Account deletedAccount = accountService.selectById(testAccount.getId());
        assertThat(deletedAccount).isNull();
    }
}