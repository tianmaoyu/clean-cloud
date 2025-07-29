package org.clean.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hi")
public class HiController {
    
    @GetMapping("/hello")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("hello");
    }

}