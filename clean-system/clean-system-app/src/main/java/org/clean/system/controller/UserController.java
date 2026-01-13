package org.clean.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.clean.Author;
import org.clean.system.entity.User;
import org.clean.system.enums.UserStatus;
import org.clean.system.param.UserAddParam;
import org.clean.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Author("admin")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @Author(value = "admin-1",date = "2020-09-01")
    @GetMapping("/getById")
    public User getById(@RequestParam("id") Long id){
        User user = userService.getById(id);
        return user;
    }

    //add
    @Author("admin-1")
    @PostMapping("/add")
    public User add(@RequestBody @Valid UserAddParam param){
        User user = new User();
        user.setName(param.getName());
        user.setPassword(param.getPassword());
        user.setAge(param.getAge());
        user.setEmail(param.getEmail());
        user.setUserType(param.getUserType());
        user.setSex(param.getSex());
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

    @PostMapping("/editLambda")
    public Boolean editLambda(@RequestParam("name") String name,@RequestParam("userId") Long userId){
        return userService.editLambda(userId,  name);
    }

    @GetMapping("/selectAll")
    public List<User> selectAll(){
        return userService.selectAll();
    }

}
