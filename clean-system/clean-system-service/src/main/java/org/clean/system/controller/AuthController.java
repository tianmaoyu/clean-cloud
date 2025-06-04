package org.clean.system.controller;

import org.clean.system.entity.User;
import org.clean.system.service.JwtTokenService;
import org.clean.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UserService userDetailsService;
    
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User request) {
        // 1. 验证用户名密码
        User userDetails = userDetailsService.getById(request.getId());

        // 2. 生成Token
        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(new User());
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody User request) {
        // 验证RefreshToken并生成新的AccessToken
        String newAccessToken = jwtTokenService.refreshAccessToken(request.getName());
        return ResponseEntity.ok(newAccessToken);
    }
}