package org.clean.system.controller;


import lombok.extern.slf4j.Slf4j;
import org.clean.Author;
import org.clean.system.entity.User;
import org.clean.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/Redis")
public class RedisController {
    private final String KEY_PREFIX = "Redis:user:id:";

    @Autowired
    private RedisTemplate<String,Object> redisTemplate ;

    @Autowired
    private UserService userService ;

    @Author("admin")
     @GetMapping("/getUser")
     public User getUser(@RequestParam("id") Long id){
         String key = KEY_PREFIX + id;
         var cacheUser= (User) redisTemplate.opsForValue().get(key);
         if(cacheUser!=null) return cacheUser;

         User user = userService.getById(id);
         Assert.notNull(user,"用户不存在");

         key= KEY_PREFIX + user.getId();
         redisTemplate.opsForValue().set(key,user,30, TimeUnit.MINUTES);
         return user;
     }

}
