package com.kalibyte.tradex.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartyLoginRequest - Request DTO for party login
 * Supports email, mobile phone, or party code (e.g. PRT-0001) as identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyLoginRequest {
    
    @NotBlank(message = "Identifier (Email, Phone, or Party Code) is required")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
}
