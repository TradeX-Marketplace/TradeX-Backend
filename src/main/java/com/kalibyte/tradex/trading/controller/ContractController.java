package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.trading.repository.ContractRepository;
import com.kalibyte.tradex.trading.entity.Contract;
import com.kalibyte.tradex.trading.dto.ContractDTO;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;

import java.util.Optional;

import com.kalibyte.tradex.common.security.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import com.kalibyte.tradex.notification.event.TradexNotificationEvents;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ContractController - Handles digital legal document operations
 * 
 * Endpoints:
 * - GET /api/v1/contracts/{id} - Get contract details
 * - POST /api/v1/contracts/{id}/sign - Sign contract (buyer/seller)
 * 
 * Contracts are auto-generated when a deal is locked.
 * They contain buyer/seller details, terms, and timestamps.
 */
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {
    
    private final ContractRepository contractRepository;
    private final WhatsAppService whatsAppService;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * GET /api/v1/contracts/{id}
     * Get contract details
     * 
     * Returns full contract including:
     * - Party information (buyer/seller names, GST INs, addresses)
     * - Deal summary (commodity, quantity, rate, total value)
     * - Terms and conditions (JSONB)
     * - Signature status
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<ContractDTO>> getContract(
            @PathVariable Long id) {
        
        Optional<Contract> contract = contractRepository.findById(id);
        
        if (contract.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ContractDTO dto = ContractDTO.fromEntity(contract.get());
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Contract retrieved successfully",
                dto
        ));
    }
    
    /**
     * POST /api/v1/contracts/{id}/sign
     * Sign a contract (buyer or seller)
     * 
     * This endpoint marks the contract as signed by the current party.
     * When both buyer and seller sign, contract status changes to COMPLETED.
     * 
     * Authorization: CAN_BUY or CAN_SELL
     */
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<ContractDTO>> signContract(
            @PathVariable Long id,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            @RequestHeader(value = "X-Party-Role", required = false) String headerRole,
            HttpServletRequest httpRequest) {
        
        Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
        
        Optional<Contract> contract = contractRepository.findById(id);
        
        if (contract.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Contract c = contract.get();
        
        // Dynamically determine if signing party is buyer or seller
        boolean isBuyer = true;
        if (c.getDeal() != null) {
            if (c.getDeal().getSellerParty() != null && partyId.equals(c.getDeal().getSellerParty().getId())) {
                isBuyer = false;
            } else if (c.getDeal().getBuyerParty() != null && partyId.equals(c.getDeal().getBuyerParty().getId())) {
                isBuyer = true;
            } else if ("SELLER".equalsIgnoreCase(headerRole)) {
                isBuyer = false;
            }
        } else if ("SELLER".equalsIgnoreCase(headerRole)) {
            isBuyer = false;
        }
        
        if (isBuyer && c.getSellerSignedAt() != null) {
            // Buyer signing after seller -> COMPLETED / LOCKED
            c.setStatus("COMPLETED");
        } else if (!isBuyer && c.getBuyerSignedAt() != null) {
            // Seller signing after buyer -> COMPLETED / LOCKED
            c.setStatus("COMPLETED");
        } else {
            // First signature -> SIGNED / APPROVED
            c.setStatus("SIGNED");
        }
        
        // Mark signature timestamp
        if (isBuyer) {
            c.setBuyerSignedAt(java.time.LocalDateTime.now());
        } else {
            c.setSellerSignedAt(java.time.LocalDateTime.now());
        }
        
        c.setUpdatedAt(java.time.LocalDateTime.now());
        
        Contract saved = contractRepository.save(c);
        
        // Publish async WhatsApp event outside transaction
        try {
            String unit = "MT";
            String link = "https://kkcanvassing.com/party-login";
            String buyerPhone = (saved.getDeal() != null && saved.getDeal().getBuyerParty() != null) ? saved.getDeal().getBuyerParty().getPhone() : null;
            String sellerPhone = (saved.getDeal() != null && saved.getDeal().getSellerParty() != null) ? saved.getDeal().getSellerParty().getPhone() : null;
            boolean isCompleted = "COMPLETED".equals(saved.getStatus());

            TradexTemplateDtos.ContractApprovedTemplate approvedTemplate = TradexTemplateDtos.ContractApprovedTemplate.builder()
                    .contractNumber(saved.getContractNumber())
                    .commodity(saved.getCommodityName())
                    .quantity(saved.getQuantity() != null ? saved.getQuantity().toPlainString() : "")
                    .unit(unit)
                    .finalRate(saved.getRate() != null ? saved.getRate().toPlainString() : "")
                    .contractLink(link)
                    .build();

            TradexTemplateDtos.ContractLockedTemplate lockedTemplate = TradexTemplateDtos.ContractLockedTemplate.builder()
                    .contractNumber(saved.getContractNumber())
                    .commodity(saved.getCommodityName())
                    .quantity(saved.getQuantity() != null ? saved.getQuantity().toPlainString() : "")
                    .unit(unit)
                    .finalRate(saved.getRate() != null ? saved.getRate().toPlainString() : "")
                    .contractLink(link)
                    .build();

            eventPublisher.publishEvent(TradexNotificationEvents.ContractSignedEvent.builder()
                    .buyerPhone(buyerPhone)
                    .sellerPhone(sellerPhone)
                    .completed(isCompleted)
                    .approvedTemplate(approvedTemplate)
                    .lockedTemplate(lockedTemplate)
                    .build());
            
            log.info("Published async WhatsApp contract signed event for contract {}", saved.getContractNumber());
        } catch (Exception e) {
            log.error("Failed to publish WhatsApp event for contract {}: {}", saved.getContractNumber(), e.getMessage());
        }
        
        ContractDTO dto = ContractDTO.fromEntity(saved);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Contract signed successfully",
                dto
        ));
    }
}
