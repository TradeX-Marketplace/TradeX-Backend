package com.kalibyte.tradex.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PartyQuickLoginRequest - Instant one-click activation & login via WhatsApp link
 * Uses partyCode (e.g. PRT-0001) received in WhatsApp template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyQuickLoginRequest {
    
    @NotBlank(message = "Party code is required")
    private String partyCode;
    
    private String phone;
    
    private String password;
}
