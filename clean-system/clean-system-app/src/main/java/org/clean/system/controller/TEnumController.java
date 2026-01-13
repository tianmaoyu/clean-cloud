package org.clean.system.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserStatus;
import org.clean.system.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/TEnum")
public class TEnumController {

    @Autowired
    private  ObjectMapper objectMapper;


    @GetMapping("/getUserType")
    public UserType getUserType(@RequestParam("userType") UserType userType){
        return userType;
    }
    @GetMapping("/getUserTypeList")
    public List<UserType> getUserTypeList(@RequestParam("userType") UserType userType){
        ArrayList<UserType> objects = new ArrayList<>();
        objects.add(UserType.USER);
        objects.add(UserType.ADMIN);
        return objects;
    }
    @GetMapping("/getSexEnum")
    public SexEnum getSexEnum(@RequestParam("sexEnum") SexEnum sexEnum){
        return sexEnum;
    }
    @GetMapping("/getUserStatus")
    public UserStatus getUserStatus(@RequestParam("userStatus") UserStatus userStatus){
        return userStatus;
    }

    @GetMapping("/getId")
    public Integer getId(@RequestParam("id") Integer id){
        return id;
    }
    @GetMapping("/getUserStatusList")
    public ArrayList<UserStatus> getUserStatusList(@RequestParam UserStatus userStatus){
        ArrayList<UserStatus> list = new ArrayList<>();
        list.add(userStatus);
        list.add(UserStatus.ENABLED);
        list.add(UserStatus.DISABLED);
        list.add(UserStatus.LOCKED);
        return list;
    }

    @SneakyThrows
    @PostMapping("/addUserStatusList")
    public ArrayList<UserStatus> addUserStatusList(@RequestBody ArrayList<UserStatus> list){
        String jsonArray = objectMapper.writeValueAsString(list);
        log.info("list:{}",jsonArray);
        return list;
    }


}
