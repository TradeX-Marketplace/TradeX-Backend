package com.kalibyte.tradex.party.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPartyRequest {
    
    private String companyName; // Required
    
    private String gstin; // Optional - GSTIN for India
    
    private String pan; // Optional - PAN for India
    
    private String contactPerson;
    
    private String email; // Required
    
    private String phone; // Required
    
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
    
    private Set<Long> roleIds; // Set of Role IDs to assign to this party

    private String status; // ACTIVE, PENDING_APPROVAL, etc.

    private String registrationType; // SELF_SIGNUP, ADMIN_CREATED, etc.

    private Set<Long> commodityIds; // Set of Commodity IDs to map to this party

    private Set<String> commodities; // Set of Commodity names / identifiers passed from frontend
}

