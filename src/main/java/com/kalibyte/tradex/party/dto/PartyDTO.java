package com.kalibyte.tradex.party.dto;

import com.kalibyte.tradex.party.dto.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyDTO {
    
    private Long id;
    
    private String partyCode;
    
    private String companyName;
    
    private String gstin;
    
    private String pan;
    
    private String contactPerson;
    
    private String email;
    
    private String phone;
    
    private String alternatePhone;
    
    private String address;
    
    private String city;
    
    private String taluka;

    private String plantLocation;

    private String state;
    
    private String country;
    
    private String postalCode;
    
    private String description;
    
    private String website;
    
    private String bankAccount;
    
    private String bankName;
    
    private String ifscCode;
    
    private Boolean active;

    private Boolean isDeleted;
    
    private Set<RoleDTO> roles;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
    
    // Registration & Approval Flow Fields
    private String status; // ACTIVE, PENDING_APPROVAL, REJECTED, CHANGES_REQUIRED
    
    private String registrationType; // SELF_SIGNUP, ADMIN_CREATED, INVITED
    
    private Long approvedBy;
    
    private LocalDateTime approvedAt;
    
    private String rejectionReason;
    
    private Long rejectedBy;
    
    private LocalDateTime rejectedAt;
    
    private String changesRequired;
    
    private Long changesRequestedBy;
    
    private LocalDateTime changesRequestedAt;

    private Set<String> commodities; // Mapped commodity names for this party
}
