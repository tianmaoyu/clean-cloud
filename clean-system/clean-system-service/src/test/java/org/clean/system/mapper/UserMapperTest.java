package org.clean.system.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;
    @Test
    void updateEmail() {

        Boolean b = userMapper.updateEmail("eric@qq.com", 1L);
        assertTrue(b);
    }
}