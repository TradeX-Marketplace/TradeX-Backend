package com.kalibyte.tradex.auth.controller;

import com.kalibyte.tradex.auth.dto.request.AdminLoginRequest;
import com.kalibyte.tradex.auth.dto.response.AdminLoginResponse;
import com.kalibyte.tradex.auth.service.AdminAuthService;
import com.kalibyte.tradex.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {
    
    private final AdminAuthService adminAuthService;
    
    /**
     * Admin login endpoint
     * POST /api/v1/admin/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Admin login request from IP: {}", ipAddress);
            
            AdminLoginResponse response = adminAuthService.loginAdmin(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(response, "Admin login successful"));
        } catch (RuntimeException e) {
            log.error("Admin login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_CREDENTIALS"));
        } catch (Exception e) {
            log.error("Admin login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Admin logout endpoint
     * POST /api/v1/admin/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> adminLogout(
            @RequestHeader("X-Admin-Id") Long adminId,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Admin logout request - Admin ID: {}", adminId);
            
            adminAuthService.logoutAdmin(adminId, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Admin logout successful"));
        } catch (Exception e) {
            log.error("Admin logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during logout", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Change password endpoint
     * POST /api/v1/admin/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("X-Admin-Id") Long adminId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Password change request for admin ID: {}", adminId);
            
            adminAuthService.changePassword(adminId, oldPassword, newPassword, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "Password updated"));
        } catch (RuntimeException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Password change error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred", "INTERNAL_ERROR"));
        }
    }

    /**
     * Update admin profile, credentials & WhatsApp alert phone
     * PUT /api/v1/admin/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<com.kalibyte.tradex.auth.entity.Admin>> updateProfile(
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            String name = body.get("name");
            String email = body.get("email");
            String phone = body.get("phone");
            String newPassword = body.get("newPassword");

            com.kalibyte.tradex.auth.entity.Admin updated = adminAuthService.updateProfile(adminId, name, email, phone, newPassword, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(updated, "Admin profile and WhatsApp alert number updated successfully"));
        } catch (RuntimeException e) {
            log.error("Profile update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Profile update error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
