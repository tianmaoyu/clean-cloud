package org.clean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private TokenType tokenType;
    private Long expiresIn;
    private String refreshToken;

    private Long userId;
    private String userName;

    private Collection<String> roles;
}