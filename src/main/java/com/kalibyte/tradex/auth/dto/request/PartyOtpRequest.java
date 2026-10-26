package com.kalibyte.tradex.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartyOtpRequest - Request WhatsApp OTP for passwordless login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    private String phone;
}
