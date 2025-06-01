package org.clean.system.service;

import org.clean.system.entity.User;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("test_user");
        testUser.setAge(25);
        testUser.setEmail("test@example.com");
        testUser.setUserType(UserType.USER);
        testUser.setSex(SexEnum.FEMALE);
    }

    @Test
    void addList() {
        // 测试添加多个用户
        int count = 3;
        Boolean result = userService.addList(count);

        assertThat(result).isTrue();

        // 验证是否添加了指定数量的用户
        List<User> users = userService.setAll();
        //最少有3个用户
        assertThat(users).hasSizeGreaterThanOrEqualTo(count);
    }

    @Test
    void edit() {
        // 先保存一个用户
        User user = new User();
        user.setName("original_name");
        user.setAge(30);
        user.setEmail("original@example.com");
        user.setUserType(UserType.USER);
        user.setSex(SexEnum.MALE);

        userService.save(user);

        // 修改用户信息
        user.setName("updated_name");
        user.setAge(35);
        user.setEmail("updated@example.com");
        user.setUserType(UserType.USER);

        int rowsAffected = userService.edit(user);

        assertThat(rowsAffected).isEqualTo(1);

        // 验证更新结果
        User updatedUser = userService.getById(user.getId());
        assertThat(updatedUser.getName()).isEqualTo("updated_name");
        assertThat(updatedUser.getAge()).isEqualTo(35);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getUserType()).isEqualTo(UserType.USER);
    }

    @Test
    void editLambda() {
        // 先保存一个用户
        User user = new User();
        user.setName("lambda_original");
        user.setAge(40);
        user.setEmail("lambda_original@example.com");
        user.setUserType(UserType.ADMIN);
        user.setSex(SexEnum.MALE);

        userService.save(user);

        // 使用 Lambda 表达式方式编辑用户
        String newName = "lambda_updated";
        Long userId = user.getId();

        Boolean result = userService.editLambda(userId, newName);

        assertThat(result).isTrue();

        // 验证更新结果
        User updatedUser = userService.getById(userId);
        assertThat(updatedUser.getName()).isEqualTo(newName);
    }

    @Test
    void getById() {
        // 保存一个用户
        User user = new User();
        user.setName("getById_test");
        user.setAge(28);
        user.setEmail("getById@example.com");
        user.setUserType(UserType.USER);
        user.setSex(SexEnum.MALE);

        userService.save(user);

        // 获取用户
        User fetchedUser = userService.getById(user.getId());

        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getName()).isEqualTo("getById_test");
        assertThat(fetchedUser.getAge()).isEqualTo(28);
        assertThat(fetchedUser.getEmail()).isEqualTo("getById@example.com");
        assertThat(fetchedUser.getUserType()).isEqualTo(UserType.USER);
    }

    @Test
    void save() {
        // 保存用户
        User user = new User();
        user.setName("save_test");
        user.setAge(32);
        user.setEmail("save@example.com");
        user.setUserType(UserType.ADMIN);
        user.setSex(SexEnum.MALE);

        User savedUser = userService.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("save_test");
        assertThat(savedUser.getAge()).isEqualTo(32);
        assertThat(savedUser.getEmail()).isEqualTo("save@example.com");
        assertThat(savedUser.getUserType()).isEqualTo(UserType.ADMIN);
    }
}