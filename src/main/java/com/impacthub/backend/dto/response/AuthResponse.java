package com.impacthub.backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String userType;
    private String role;
    private Long userId;
    private String fullName;
    private String email;
    private String message;

    public AuthResponse(String token, String userType, String email, String message) {
        this.token = token;
        this.userType = userType;
        this.role = userType;
        this.email = email;
        this.message = message;
    }

    public AuthResponse(String token, String userType, Long userId, String fullName, String email, String message) {
        this.token = token;
        this.userType = userType;
        this.role = userType;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.message = message;
    }
}
