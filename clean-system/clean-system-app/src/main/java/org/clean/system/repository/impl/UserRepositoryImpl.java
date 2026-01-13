package org.clean.system.repository.impl;

import org.clean.system.entity.User;
import org.clean.system.mapper.UserMapper;
import org.clean.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User insert(User user) {
        int insert = userMapper.insert(user);
        return insert > 0 ? user : null;
    }

    @Override
    public User insert(List<User> userList) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public User update(List<User> userList) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Boolean delete(List<Long> idList) {
        return null;
    }

    @Override
    public User select(Long id) {
        return null;
    }

    @Override
    public List<User> select(List<Long> idList) {
        return List.of();
    }

    @Override
    public User select(String name) {
        return null;
    }
}
