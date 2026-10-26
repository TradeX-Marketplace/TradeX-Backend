package com.kalibyte.tradex.auth.controller;

import com.kalibyte.tradex.auth.dto.request.PartyLoginRequest;
import com.kalibyte.tradex.auth.dto.request.PartyOtpRequest;
import com.kalibyte.tradex.auth.dto.request.PartyOtpVerifyRequest;
import com.kalibyte.tradex.auth.dto.request.PartyQuickLoginRequest;
import com.kalibyte.tradex.auth.dto.response.PartyLoginResponse;
import com.kalibyte.tradex.auth.service.PartyAuthService;
import com.kalibyte.tradex.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PartyAuthController - REST Controller providing the fastest and most flexible
 * authentication endpoints for TradeX trading parties (Buyers, Sellers, Traders).
 */
@RestController
@RequestMapping("/api/v1/auth/party")
@RequiredArgsConstructor
@Slf4j
public class PartyAuthController {

    private final PartyAuthService partyAuthService;

    /**
     * Standard Party Login
     * POST /api/v1/auth/party/login
     * Supports Email, Phone Number, or Party Code (PRT-XXXX) + Password.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PartyLoginResponse>> login(
            @Valid @RequestBody PartyLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Party login request for identifier [{}] from IP: {}", request.getIdentifier(), ipAddress);
            PartyLoginResponse response = partyAuthService.login(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (RuntimeException e) {
            log.warn("Party login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_CREDENTIALS"));
        } catch (Exception e) {
            log.error("Unexpected error during party login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication service unavailable", "INTERNAL_ERROR"));
        }
    }

    /**
     * Fastest Way: 1-Click WhatsApp Link Activation & Quick Login
     * POST /api/v1/auth/party/quick-login (and POST /api/v1/auth/party/activate)
     * Consumes partyCode from WhatsApp activation link (e.g. PRT-0001) and returns JWT instantly.
     */
    @PostMapping({"/quick-login", "/activate"})
    public ResponseEntity<ApiResponse<PartyLoginResponse>> quickLoginOrActivate(
            @Valid @RequestBody PartyQuickLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Party quick-login / activation request for code [{}] from IP: {}", request.getPartyCode(), ipAddress);
            PartyLoginResponse response = partyAuthService.quickLoginOrActivate(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(response, "Account activated and logged in successfully"));
        } catch (RuntimeException e) {
            log.warn("Party quick login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "ACTIVATION_FAILED"));
        } catch (Exception e) {
            log.error("Unexpected error during party quick login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Activation service unavailable", "INTERNAL_ERROR"));
        }
    }

    /**
     * Request WhatsApp OTP for Passwordless Mobile Login
     * POST /api/v1/auth/party/otp/request
     */
    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<String>> requestOtp(
            @Valid @RequestBody PartyOtpRequest request) {
        try {
            log.info("Party WhatsApp OTP requested for phone: {}", request.getPhone());
            partyAuthService.requestOtp(request);
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully to your WhatsApp number"));
        } catch (RuntimeException e) {
            log.warn("OTP request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "OTP_REQUEST_FAILED"));
        } catch (Exception e) {
            log.error("Unexpected error during OTP request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to dispatch OTP", "INTERNAL_ERROR"));
        }
    }

    /**
     * Verify WhatsApp OTP and Login
     * POST /api/v1/auth/party/otp/verify
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<PartyLoginResponse>> verifyOtp(
            @Valid @RequestBody PartyOtpVerifyRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            log.info("Verifying WhatsApp OTP for phone [{}] from IP: {}", request.getPhone(), ipAddress);
            PartyLoginResponse response = partyAuthService.verifyOtp(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully. Login successful."));
        } catch (RuntimeException e) {
            log.warn("OTP verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_OTP"));
        } catch (Exception e) {
            log.error("Unexpected error during OTP verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Verification service unavailable", "INTERNAL_ERROR"));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
