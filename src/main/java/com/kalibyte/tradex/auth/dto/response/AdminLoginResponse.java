package com.kalibyte.tradex.auth.dto.response;

import com.kalibyte.tradex.auth.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginResponse {
    
    private Long adminId;
    
    private String adminCode;
    
    private String name;
    
    private String email;
    
    private String role; // SUPER_ADMIN, ADMIN, MODERATOR
    
    private Boolean active;
    
    private String token;
    
    private String refreshToken;
    
    private Long expiresIn; // in milliseconds
    
    private LocalDateTime loginTime;
    
    private LocalDateTime lastLogin;
    
    public static AdminLoginResponse fromAdmin(Admin admin, String token, String refreshToken, Long expiresIn) {
        return AdminLoginResponse.builder()
                .adminId(admin.getId())
                .adminCode(admin.getAdminCode())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .active(admin.getActive())
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .loginTime(LocalDateTime.now())
                .lastLogin(admin.getLastLogin())
                .build();
    }
}
