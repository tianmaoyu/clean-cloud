package org.clean.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hi")
public class HiController {
    
    @GetMapping("/hello")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("hello");
    }

}