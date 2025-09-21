package org.clean.system.repository;

import org.clean.system.entity.User;

import java.util.List;

public interface UserRepository {

    public User insert(User user);
    public User insert(List<User> userList);

    public User update(User user);
    public User update(List<User> userList);
    public Boolean delete(Long id);
    public Boolean delete(List<Long> idList);

    public User select(Long id);
    public List<User> select(List<Long> idList);
    public User select(String name);
}
