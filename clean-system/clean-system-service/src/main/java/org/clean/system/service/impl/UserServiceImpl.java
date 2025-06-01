package org.clean.system.service.impl;

import lombok.SneakyThrows;
import org.clean.system.entity.User;
import org.clean.system.mapper.UserMapper;
import org.clean.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author eric
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-05-21 21:28:51
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public Boolean addList(Integer count) {
        ArrayList<User> users = new ArrayList<>();
        for (int i=0;i<10;i++) {
            User u = new User();
            u.setName("eric"+i);
            u.setAge(i);
            u.setEmail("eric"+i+"@qq.com");
            users.add(u);
        }
        boolean saved = userMapper.insertBatch(users);
        return saved;
    }

    @Override
    public int edit(User user) {
        return userMapper.updateById(user);
    }




    @Override
    public Boolean editLambda(Long id, String name) {

        var date = parseStringToDate("2025-05-23 14:30:00");

        var update = userMapper.updateNameById(name,id);

        return update;
    }

    @Override
    public User getById(Long id) {
        return userMapper.getById(id);
    }

    @Override
    public User save(User user) {
        userMapper.insert(user);
        return user;
    }

    @SneakyThrows
    public static Date parseStringToDate(String dateStr) {
        var sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        return sdf.parse(dateStr);
    }



}




