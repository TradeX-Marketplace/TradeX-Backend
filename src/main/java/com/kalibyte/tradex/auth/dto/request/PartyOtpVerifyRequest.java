package com.kalibyte.tradex.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartyOtpVerifyRequest - Verify WhatsApp OTP for passwordless login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyOtpVerifyRequest {
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    @NotBlank(message = "OTP is required")
    private String otp;
}
