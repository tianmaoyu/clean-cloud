package org.clean.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.clean.mybatis.BatchUtils;
import org.clean.system.entity.User;
import org.clean.system.enums.UserType;

import java.util.ArrayList;
import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {
    default LambdaUpdateChainWrapper<User> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(this);
    }

    default LambdaQueryChainWrapper<User> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(this);
    }

    default User getById(Long id) {
        return this.selectById(id);

    }

    default Boolean insertBatch(List<User> users){
        Boolean success = BatchUtils.insertBatch(this, users);
        return success;
    }


    default Boolean updateEmail(String email, Long id) {
        return lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getEmail, email)
                .set(User::getUserType, UserType.ADMIN)
                .update(new User());
    }

    default User update(User user) {
        return this.update(user);
    }

    default int deleteByName(String name) {
        var wrapper = lambdaQuery().eq(User::getName, name);
        return this.delete(wrapper);
    }

    default boolean insertBatch(ArrayList<User> users) {
        this.insert(users);
        return true;
    }

    default boolean updateNameById(String name, Long id) {
        return lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getName, name)
                .update(new User());
    }

    List<User> selectAll();

}




