package com.kalibyte.tradex.party.service;

import com.kalibyte.tradex.party.dto.PartyDTO;
import com.kalibyte.tradex.party.dto.request.AddPartyRequest;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.party.entity.Party;
import com.kalibyte.tradex.party.entity.PartyStatus;
import com.kalibyte.tradex.party.entity.Role;
import com.kalibyte.tradex.party.repository.PartyRepository;
import com.kalibyte.tradex.party.service.RoleService;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;
import com.kalibyte.tradex.party.entity.PartyCommodityMapping;
import com.kalibyte.tradex.party.repository.PartyCommodityMappingRepository;
import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.commodity.repository.CommodityRepository;
import com.kalibyte.tradex.notification.config.WhatsAppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import com.kalibyte.tradex.notification.event.TradexNotificationEvents;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyService {
    
    private final PartyRepository partyRepository;
    private final RoleService roleService;
    private final WhatsAppService whatsAppService;
    private final WhatsAppConfig whatsAppConfig;
    private final ApplicationEventPublisher eventPublisher;
    private final PartyCommodityMappingRepository partyCommodityMappingRepository;
    private final CommodityRepository commodityRepository;
    
    /**
     * Add a new party - Admin only operation
     */
    @Transactional
    public PartyDTO addParty(AddPartyRequest request, String createdBy) {
        // Validate required fields
        validatePartyRequest(request);
        
        // Check for duplicate email only if provided
        String cleanEmail = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null;
        if (cleanEmail != null && partyRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Party with this email already exists");
        }
        
        // Check for duplicate phone
        if (partyRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Party with this phone already exists");
        }
        
        // Check for duplicate GSTIN if provided
        if (request.getGstin() != null && !request.getGstin().isEmpty()) {
            if (partyRepository.findByGstin(request.getGstin()).isPresent()) {
                throw new RuntimeException("Party with this GSTIN already exists");
            }
        }
        
        // Generate party code
        String partyCode = generatePartyCode();
        
        // Get roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = roleService.getRolesByIds(request.getRoleIds());
        }
        
        // Status and registration type handling
        String partyStatus = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
                ? request.getStatus().trim().toUpperCase() : "ACTIVE";
        
        String inputRegType = (request.getRegistrationType() != null && !request.getRegistrationType().trim().isEmpty())
                ? request.getRegistrationType().trim().toUpperCase() : null;
        
        boolean isPending = "PENDING_APPROVAL".equalsIgnoreCase(partyStatus) || "PENDING".equalsIgnoreCase(partyStatus);
        boolean isActive = "ACTIVE".equalsIgnoreCase(partyStatus);

        String regType;
        if (inputRegType != null) {
            regType = ("SELF_SIGNUP".equals(inputRegType) || "SELF_REGISTERED".equals(inputRegType))
                    ? "SELF_REGISTERED"
                    : inputRegType;
        } else {
            regType = isPending ? "SELF_REGISTERED" : "ADMIN_CREATED";
        }

        // Create party
        Party party = Party.builder()
                .partyCode(partyCode)
                .companyName(request.getCompanyName())
                .gstin(request.getGstin())
                .pan(request.getPan())
                .contactPerson(request.getContactPerson())
                .email(cleanEmail)
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .address(request.getAddress())
                .city(request.getCity())
                .taluka(request.getTaluka())
                .plantLocation(request.getPlantLocation())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .description(request.getDescription())
                .website(request.getWebsite())
                .bankAccount(request.getBankAccount())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .status(partyStatus)
                .registrationType(regType)
                .active(isActive)
                .canLogin(isActive)
                .requiresApproval(isPending)
                .roles(roles)
                .createdBy(createdBy)
                .build();
        
        Party savedParty = partyRepository.save(party);
        
        // Save commodity mappings if provided
        savePartyCommodityMappings(savedParty.getId(), request, createdBy);

        // If party is registered in PENDING_APPROVAL status (e.g. self-registration)
        // Alert admin to review and approve the new party registration
        if (isPending) {
            try {
                String adminPhone = whatsAppConfig.getAdminPhone();
                if (adminPhone != null && !adminPhone.trim().isEmpty()) {
                    String contactPerson = (savedParty.getContactPerson() != null && !savedParty.getContactPerson().trim().isEmpty())
                            ? savedParty.getContactPerson() : savedParty.getCompanyName();
                    String commoditiesStr = getCommodityNamesForParty(savedParty.getId()).stream().collect(Collectors.joining(", "));
                    String adminMsg = String.format(
                            "📢 *K.K.CANVANSSING AGENT | NEW PARTY REGISTRATION*\n\n" +
                            "A new party has submitted registration and is *AWAITING APPROVAL*:\n\n" +
                            "• *Party:* %s (%s)\n" +
                            "• *Contact:* %s (%s)\n" +
                            "• *Location:* %s, %s\n" +
                            (commoditiesStr.isEmpty() ? "" : "• *Commodities:* " + commoditiesStr + "\n") +
                            "\n👉 *REVIEW & APPROVE:*\nhttps://kkcanvassing.com/admin/party-approvals",
                            savedParty.getCompanyName().toUpperCase(),
                            savedParty.getPartyCode(),
                            contactPerson,
                            savedParty.getPhone(),
                            savedParty.getTaluka() != null ? savedParty.getTaluka() : (savedParty.getCity() != null ? savedParty.getCity() : ""),
                            savedParty.getState() != null ? savedParty.getState() : "India"
                    );
                    whatsAppService.sendMessage(adminPhone, adminMsg, null, "ADMIN_ALERT");
                    log.info("[Async WhatsApp] Admin alerted for new pending registration: {} ({})", savedParty.getCompanyName(), savedParty.getPartyCode());
                }
            } catch (Exception e) {
                log.warn("Failed to dispatch admin WhatsApp alert for pending registration {}: {}", savedParty.getPartyCode(), e.getMessage());
            }
        }

        // Automatically dispatch WhatsApp Party Activation template immediately ONLY if already ACTIVE (e.g., admin added)
        if (isActive) {
            try {
                Set<String> targetPhones = savedParty.getAllContactPhones();
                if (!targetPhones.isEmpty()) {
                    String recipientName = (savedParty.getContactPerson() != null && !savedParty.getContactPerson().trim().isEmpty())
                            ? savedParty.getContactPerson() : savedParty.getCompanyName();
                    String activationLink = "https://kkcanvassing.com/party-login";
                    
                    TradexTemplateDtos.PartyActivationTemplate activationTemplate = TradexTemplateDtos.PartyActivationTemplate.builder()
                            .partyName(recipientName)
                            .secureLink(activationLink)
                            .businessName("K.K.CANVANSSING AGENT")
                            .primaryMobile(savedParty.getPhone())
                            .build();
                    
                    for (String targetPhone : targetPhones) {
                        eventPublisher.publishEvent(new TradexNotificationEvents.PartyActivationEvent(targetPhone, activationTemplate));
                        log.info("Published async WhatsApp party activation event for {} ({})", targetPhone, savedParty.getPartyCode());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to publish WhatsApp party activation event for party {}: {}", savedParty.getPartyCode(), e.getMessage());
                // Notification failure should not fail account creation
            }
        }
        
        return convertToDTO(savedParty);
    }
    
    /**
     * Get party by ID
     */
    public PartyDTO getPartyById(Long partyId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        return convertToDTO(party);
    }
    
    /**
     * Get party by party code
     */
    public PartyDTO getPartyByCode(String partyCode) {
        Party party = partyRepository.findByPartyCode(partyCode)
                .orElseThrow(() -> new RuntimeException("Party not found with code: " + partyCode));
        return convertToDTO(party);
    }
    
    /**
     * Get all parties with pagination
     * Defaults to active/non-deleted parties. If showDeleted = true, returns only soft-deleted parties.
     */
    public Page<PartyDTO> getAllParties(Pageable pageable) {
        return getAllParties(pageable, false);
    }

    public Page<PartyDTO> getAllParties(Pageable pageable, Boolean showDeleted) {
        if (Boolean.TRUE.equals(showDeleted)) {
            return partyRepository.findByIsDeletedTrue(pageable)
                    .map(this::convertToDTO);
        }
        return partyRepository.findByIsDeletedFalse(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Update party (Admin only)
     */
    @Transactional
    public PartyDTO updateParty(Long partyId, AddPartyRequest request, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        // Check email uniqueness if changed
        String cleanEmail = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null;
        if (cleanEmail != null
                && !java.util.Objects.equals(party.getEmail(), cleanEmail)
                && partyRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Party with this email already exists");
        }
        
        // Check phone uniqueness if changed
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()
                && !java.util.Objects.equals(party.getPhone(), request.getPhone())
                && partyRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Party with this phone already exists");
        }

        // Track existing contact numbers before update
        Set<String> oldPhones = party.getAllContactPhones();
        
        // Update fields
        party.setCompanyName(request.getCompanyName());
        party.setGstin(request.getGstin());
        party.setPan(request.getPan());
        party.setContactPerson(request.getContactPerson());
        party.setEmail(cleanEmail);
        party.setPhone(request.getPhone());
        party.setAlternatePhone(request.getAlternatePhone());
        party.setAddress(request.getAddress());
        party.setCity(request.getCity());
        party.setTaluka(request.getTaluka());
        party.setPlantLocation(request.getPlantLocation());
        party.setState(request.getState());
        party.setCountry(request.getCountry());
        party.setPostalCode(request.getPostalCode());
        party.setDescription(request.getDescription());
        party.setWebsite(request.getWebsite());
        party.setBankAccount(request.getBankAccount());
        party.setBankName(request.getBankName());
        party.setIfscCode(request.getIfscCode());
        
        // Update roles if provided
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = roleService.getRolesByIds(request.getRoleIds());
            party.setRoles(roles);
        }
        
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);

        // When admin adds more numbers while editing, dispatch activation message ONLY to that extra number
        Set<String> newPhones = updatedParty.getAllContactPhones();
        Set<String> newlyAddedPhones = new java.util.LinkedHashSet<>(newPhones);
        newlyAddedPhones.removeAll(oldPhones);

        if (!newlyAddedPhones.isEmpty()) {
            try {
                String recipientName = (updatedParty.getContactPerson() != null && !updatedParty.getContactPerson().trim().isEmpty())
                        ? updatedParty.getContactPerson() : updatedParty.getCompanyName();
                String activationLink = "https://kkcanvassing.com/party-login";
                
                TradexTemplateDtos.PartyActivationTemplate activationTemplate = TradexTemplateDtos.PartyActivationTemplate.builder()
                        .partyName(recipientName)
                        .secureLink(activationLink)
                        .businessName("K.K.CANVANSSING AGENT")
                        .primaryMobile(updatedParty.getPhone())
                        .build();
                
                for (String extraPhone : newlyAddedPhones) {
                    eventPublisher.publishEvent(new TradexNotificationEvents.PartyActivationEvent(extraPhone, activationTemplate));
                    log.info("Published async WhatsApp party activation event for newly added number {} ({})", extraPhone, updatedParty.getPartyCode());
                }
            } catch (Exception e) {
                log.error("Failed to publish WhatsApp party activation for newly added numbers {}: {}", newlyAddedPhones, e.getMessage());
            }
        }

        return convertToDTO(updatedParty);
    }
    
    /**
     * Deactivate party (Admin only)
     */
    @Transactional
    public PartyDTO deactivateParty(Long partyId, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        party.setActive(false);
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);
        return convertToDTO(updatedParty);
    }
    
    /**
     * Soft Delete party (Admin only)
     * Sets isDeleted = true, status = DELETED, active = false, canLogin = false
     */
    @Transactional
    public PartyDTO deleteParty(Long partyId, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        party.setIsDeleted(true);
        party.setActive(false);
        party.setCanLogin(false);
        party.setStatus(PartyStatus.DELETED.name());
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);
        log.info("Party [{}] ({}) marked as soft-deleted by [{}]", party.getPartyCode(), party.getCompanyName(), updatedBy);
        return convertToDTO(updatedParty);
    }

    /**
     * Restore soft-deleted party (Admin only)
     */
    @Transactional
    public PartyDTO restoreParty(Long partyId, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        party.setIsDeleted(false);
        party.setActive(true);
        party.setCanLogin(true);
        party.setStatus(PartyStatus.ACTIVE.name());
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);
        log.info("Party [{}] ({}) restored by [{}]", party.getPartyCode(), party.getCompanyName(), updatedBy);
        return convertToDTO(updatedParty);
    }
    
    /**
     * Activate party (Admin only)
     */
    @Transactional
    public PartyDTO activateParty(Long partyId, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        party.setStatus("ACTIVE");
        party.setActive(true);
        party.setCanLogin(true);
        party.setRequiresApproval(false);
        party.setApprovedAt(LocalDateTime.now());
        try {
            party.setApprovedBy(Long.parseLong(updatedBy));
        } catch (Exception ignored) {
            party.setApprovedBy(1L);
        }
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);

        // Automatically dispatch WhatsApp Party Activation template on admin approval
        try {
            Set<String> targetPhones = updatedParty.getAllContactPhones();
            if (!targetPhones.isEmpty()) {
                String recipientName = (updatedParty.getContactPerson() != null && !updatedParty.getContactPerson().trim().isEmpty())
                        ? updatedParty.getContactPerson() : updatedParty.getCompanyName();
                String activationLink = "https://kkcanvassing.com/party-login";
                
                TradexTemplateDtos.PartyActivationTemplate activationTemplate = TradexTemplateDtos.PartyActivationTemplate.builder()
                        .partyName(recipientName)
                        .secureLink(activationLink)
                        .businessName("K.K.CANVANSSING AGENT")
                        .primaryMobile(updatedParty.getPhone())
                        .build();
                
                for (String targetPhone : targetPhones) {
                    eventPublisher.publishEvent(new TradexNotificationEvents.PartyActivationEvent(targetPhone, activationTemplate));
                    log.info("Published async WhatsApp party activation event on approval for {} ({})", targetPhone, updatedParty.getPartyCode());
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish WhatsApp party activation on approval for party {}: {}", updatedParty.getPartyCode(), e.getMessage());
        }

        return convertToDTO(updatedParty);
    }

    /**
     * Reject party (Admin only)
     */
    @Transactional
    public PartyDTO rejectParty(Long partyId, String reason, String updatedBy) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with ID: " + partyId));
        
        String finalReason = (reason != null && !reason.trim().isEmpty())
                ? reason.trim() : "Verification criteria not satisfied";

        party.setStatus("REJECTED");
        party.setActive(false);
        party.setCanLogin(false);
        party.setRequiresApproval(false);
        party.setRejectionReason(finalReason);
        party.setRejectedAt(LocalDateTime.now());
        try {
            party.setRejectedBy(Long.parseLong(updatedBy));
        } catch (Exception ignored) {
            party.setRejectedBy(1L);
        }
        party.setUpdatedBy(updatedBy);
        
        Party updatedParty = partyRepository.save(party);

        // Automatically dispatch WhatsApp rejection message
        try {
            Set<String> targetPhones = updatedParty.getAllContactPhones();
            if (!targetPhones.isEmpty()) {
                String recipientName = (updatedParty.getContactPerson() != null && !updatedParty.getContactPerson().trim().isEmpty())
                        ? updatedParty.getContactPerson() : updatedParty.getCompanyName();
                
                String rejectionMessage = String.format(
                        "*K.K.CANVANSSING AGENT | REGISTRATION STATUS*\n\n" +
                        "DEAR %s,\n\n" +
                        "YOUR APPLICATION FOR *%s* (%s) HAS BEEN REVIEWED AND COULD NOT BE APPROVED AT THIS TIME.\n\n" +
                        "REASON FOR REJECTION:\n" +
                        "\"%s\"\n\n" +
                        "IF YOU HAVE QUESTIONS OR WISH TO RE-APPLY, PLEASE CONTACT THE K.K.CANVANSSING AGENT DESK.\n\n" +
                        "_K.K.CANVANSSING AGENT DESK • KKCANVASSING.COM_",
                        recipientName.toUpperCase(),
                        updatedParty.getCompanyName().toUpperCase(),
                        updatedParty.getPartyCode(),
                        finalReason
                );

                for (String targetPhone : targetPhones) {
                    try {
                        whatsAppService.sendMessage(targetPhone, rejectionMessage, String.valueOf(updatedParty.getId()), "PARTY_REJECTION");
                        log.info("Sent WhatsApp rejection notice for {} ({})", targetPhone, updatedParty.getPartyCode());
                    } catch (Exception msgEx) {
                        log.warn("Could not send direct rejection message to {}: {}", targetPhone, msgEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to dispatch WhatsApp rejection for party {}: {}", updatedParty.getPartyCode(), e.getMessage());
        }

        return convertToDTO(updatedParty);
    }
    
    /**
     * Validate party request
     */
    private void validatePartyRequest(AddPartyRequest request) {
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }
        
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Phone is required");
        }
    }
    
    /**
     * Generate unique party code
     * Format: PRT-XXXX (short format)
     * Example: PRT-0001, PRT-0002, ..., PRT-9999
     * 
     * Simple 4-digit sequence with collision detection
     */
    private String generatePartyCode() {
        try {
            // Get sequence from database
            long sequence = partyRepository.countAllParties() + 1;
            
            // Generate code with 4-digit format: PRT-0001
            String partyCode = String.format("PRT-%04d", sequence % 10000);
            
            // Verify uniqueness (in case of concurrent creation)
            int retryCount = 0;
            while (partyRepository.findByPartyCode(partyCode).isPresent() && retryCount < 5) {
                sequence++;
                partyCode = String.format("PRT-%04d", sequence % 10000);
                retryCount++;
            }
            
            return partyCode;
        } catch (Exception e) {
            // Fallback: use UUID if something goes wrong
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            return String.format("PRT-%s", uuid);
        }
    }
    
    /**
     * Convert Party entity to PartyDTO
     */
    public PartyDTO convertToDTO(Party party) {
        return PartyDTO.builder()
                .id(party.getId())
                .partyCode(party.getPartyCode())
                .companyName(party.getCompanyName())
                .gstin(party.getGstin())
                .pan(party.getPan())
                .contactPerson(party.getContactPerson())
                .email(party.getEmail())
                .phone(party.getPhone())
                .alternatePhone(party.getAlternatePhone())
                .address(party.getAddress())
                .city(party.getCity())
                .taluka(party.getTaluka())
                .plantLocation(party.getPlantLocation())
                .state(party.getState())
                .country(party.getCountry())
                .postalCode(party.getPostalCode())
                .description(party.getDescription())
                .website(party.getWebsite())
                .bankAccount(party.getBankAccount())
                .bankName(party.getBankName())
                .ifscCode(party.getIfscCode())
                .status(party.getStatus())
                .registrationType(party.getRegistrationType())
                .approvedBy(party.getApprovedBy())
                .approvedAt(party.getApprovedAt())
                .rejectedBy(party.getRejectedBy())
                .rejectedAt(party.getRejectedAt())
                .rejectionReason(party.getRejectionReason())
                .changesRequired(party.getChangesRequired())
                .changesRequestedBy(party.getChangesRequestedBy())
                .changesRequestedAt(party.getChangesRequestedAt())
                .active(party.getActive())
                .isDeleted(party.getIsDeleted())
                .roles(party.getRoles().stream()
                        .map(roleService::convertToDTO)
                        .collect(Collectors.toSet()))
                .createdBy(party.getCreatedBy())
                .createdAt(party.getCreatedAt())
                .updatedBy(party.getUpdatedBy())
                .updatedAt(party.getUpdatedAt())
                .commodities(getCommodityNamesForParty(party.getId()))
                .build();
    }

    /**
     * Helper to resolve and save party commodity mappings
     */
    private void savePartyCommodityMappings(Long partyId, AddPartyRequest request, String createdBy) {
        Set<Long> targetCommodityIds = new HashSet<>();

        if (request.getCommodityIds() != null && !request.getCommodityIds().isEmpty()) {
            targetCommodityIds.addAll(request.getCommodityIds());
        }

        if (request.getCommodities() != null && !request.getCommodities().isEmpty()) {
            for (String commodityNameOrCode : request.getCommodities()) {
                if (commodityNameOrCode == null || commodityNameOrCode.trim().isEmpty()) continue;
                String trimmed = commodityNameOrCode.trim();
                commodityRepository.findByCommodityName(trimmed)
                        .or(() -> commodityRepository.findByCommodityCode(trimmed))
                        .ifPresent(c -> targetCommodityIds.add(c.getId()));
            }
        }

        if (targetCommodityIds.isEmpty()) return;

        for (Long commodityId : targetCommodityIds) {
            boolean exists = partyCommodityMappingRepository
                    .findByPartyIdAndCommodityIdAndCapabilityType(partyId, commodityId, "BOTH")
                    .isPresent();

            if (!exists) {
                PartyCommodityMapping mapping = PartyCommodityMapping.builder()
                        .partyId(partyId)
                        .commodityId(commodityId)
                        .capabilityType("BOTH")
                        .notificationEnabled(true)
                        .status("ACTIVE")
                        .createdBy(createdBy)
                        .build();
                partyCommodityMappingRepository.save(mapping);
            }
        }
    }

    /**
     * Retrieve set of mapped commodity names for a given party
     */
    private Set<String> getCommodityNamesForParty(Long partyId) {
        if (partyId == null) return java.util.Collections.emptySet();
        try {
            return partyCommodityMappingRepository.findByPartyIdAndStatus(partyId, "ACTIVE").stream()
                    .map(m -> commodityRepository.findById(m.getCommodityId())
                            .map(Commodity::getCommodityName)
                            .orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Failed to fetch commodities for party {}: {}", partyId, e.getMessage());
            return java.util.Collections.emptySet();
        }
    }
}
