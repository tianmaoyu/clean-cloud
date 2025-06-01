package org.clean.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clean.system.entity.User;
import org.clean.system.enums.UserStatus;
import org.clean.system.param.UserAddParam;
import org.clean.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @GetMapping("/getById")
    public User getById(){
        User user = userService.getById(1L);
        return user;
    }

    //add
    @PostMapping("/add")
    public User add(@RequestBody UserAddParam param){
        User user = new User();
        user.setName(param.getName());
        user.setAge(param.getAge());
        user.setEmail(param.getEmail());
        userService.save(user);
        return user;
    }

    @PostMapping("/addList")
    public Boolean addList(@RequestParam("count") Integer count){
       return userService.addList(count);
    }
    //edit
    @PostMapping("/edit")
    public Integer edit(@RequestBody User user){
       return userService.edit(user);
    }
    @PostMapping("/edit2")
    public Integer edit2( User user){
        return userService.edit(user);
    }
    @PostMapping("/editLambda")
    public Boolean editLambda(@RequestParam("name") String name,@RequestParam("userId") Long userId){
        return userService.editLambda(userId,  name);
    }



}
