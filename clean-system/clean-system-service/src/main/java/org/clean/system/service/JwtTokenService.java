package org.clean.system.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.clean.system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {
    
    @Value("${jwt.secret:123456}")
    private String secret;
    
    @Value("${jwt.access-token-expiration:3600}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:86400}")
    private Long refreshTokenExpiration;


    @Autowired
    private UserService userDetailsService;
    
    // 生成AccessToken
    public String generateAccessToken(User userDetails) {
        return buildToken(userDetails, accessTokenExpiration);
    }
    
    // 生成RefreshToken
    public String generateRefreshToken(User userDetails) {
        return buildToken(userDetails, refreshTokenExpiration);
    }
    
    private String buildToken(User userDetails, Long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());

        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    // 刷新AccessToken
    public String refreshAccessToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(refreshToken)
                    .getBody();
            
            String id = claims.getSubject();
            User userDetails = userDetailsService.getById(Long.parseLong(id));
            
            return generateAccessToken(userDetails);
        } catch (Exception e) {
            throw new RuntimeException("无效的RefreshToken");
        }
    }
    
    // 验证Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从Token中获取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}