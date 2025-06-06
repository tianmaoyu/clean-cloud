package org.clean.system.converter;

import lombok.SneakyThrows;
import org.clean.system.entity.User;
import org.clean.system.param.UserParam;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Mapper(componentModel = "spring")
public interface UserConverter {
    UserParam toParam(User entity);
    List<UserParam> toParam(List<User> entityList);

    User toEntity(UserParam param);
    List<User> toEntity(List<UserParam> paramList);


    // 字段名称一样,类型不一样
    default String toString(Date date){

        if (date == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(date);
    }
    // 字段名称一样,类型不一样
    @SneakyThrows
    default Date toDate(String dateStr){

        if (dateStr == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.parse(dateStr);
    }

    // 字段名称 不一样,类型一样
    // 字段名称 不一样,类型也不一样
    @AfterMapping
    default void toParamAfterMapping(User entity,@MappingTarget UserParam target){
        target.setUpdateUserName(entity.getName()+entity.getUpdateId());
    }
}
