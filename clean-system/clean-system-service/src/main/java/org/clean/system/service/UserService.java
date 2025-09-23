package org.clean.system.service;


import org.clean.system.entity.User;

import java.util.List;

/**
* @author eric
* @description 针对表【user】的数据库操作Service
* @createDate 2025-05-21 21:28:51
*/
public interface UserService  {

    Boolean addList(Integer count);
    int edit(User user);

    Boolean editLambda(Long id, String name);

    User getById(Long id);

    User save(User user);

    List<User> setAll();

    List<User> selectAll();

}
