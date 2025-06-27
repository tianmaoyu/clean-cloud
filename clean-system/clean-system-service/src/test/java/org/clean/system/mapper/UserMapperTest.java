package org.clean.system.mapper;

import lombok.extern.slf4j.Slf4j;
import org.clean.system.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;
    @Transactional
    @Test
    void updateEmail() {

        Boolean b = userMapper.updateEmail("eric@qq.com", 1L);
        assertTrue(b);
    }

    @Transactional
    @Test
    void insertBatch() {

        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("insertBatch-eric"+i);
            user.setAge(i);
            user.setEmail("insertBatch-eric"+i+"@qq.com");
            userList.add(user);
        }

        long start = System.currentTimeMillis();

        Boolean result = userMapper.insertBatch(userList);

        long timing = System.currentTimeMillis() - start;
        log.info("【--------】实际执行耗时: {}ms", timing);
        assertTrue(result);
    }

    @Transactional
    @Test
    void insertBatch2() {

        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("insertBatch-eric"+i);
            user.setAge(i);
            user.setEmail("insertBatch-eric"+i+"@qq.com");
            userList.add(user);
        }

        long start = System.currentTimeMillis();

        for (User user : userList) {
             userMapper.insert(user);
        }
        long timing = System.currentTimeMillis() - start;
        log.info("【--------】实际执行耗时: {}ms", timing);
    }

    @Transactional
    @Test
    void insertBatch3() {

        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName("insertBatch-eric"+i);
            user.setAge(i);
            user.setEmail("insertBatch-eric"+i+"@qq.com");
            userList.add(user);
        }

        for (User user : userList) {
            userMapper.insert(user);
        }

    }
}