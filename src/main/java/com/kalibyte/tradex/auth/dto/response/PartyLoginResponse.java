package com.kalibyte.tradex.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * PartyLoginResponse - Response DTO returned upon successful party authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyLoginResponse {
    
    private String token;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    private Long partyId;
    private String partyCode;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String primaryRole;
    private Set<String> roles;
    private String status;
}
