package org.clean.inventory.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/Hello")
public class HelloController {

    @GetMapping("/hi")
    public String hi(){
        return  "hi";
    }
}
