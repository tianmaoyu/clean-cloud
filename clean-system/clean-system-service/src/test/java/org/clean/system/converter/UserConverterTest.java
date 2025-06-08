package org.clean.system.converter;

import lombok.extern.slf4j.Slf4j;
import org.clean.system.entity.User;
import org.clean.system.param.UserParam;
import org.clean.system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class UserConverterTest {

    @Autowired
    private UserConverter userConverter ;

    @Autowired
    private UserService userService ;

    @Test
    void toParam() {

        User user = userService.getById(1L);
        UserParam param = userConverter.toParam(user);
        log.info("param:{}",param);
        //这个字段是字符串
        assertEquals(String.class,param.getUpdateTime().getClass());
    }
}