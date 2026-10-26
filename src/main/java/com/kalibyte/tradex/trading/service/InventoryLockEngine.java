package com.kalibyte.tradex.trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kalibyte.tradex.trading.dto.DealDTO;
import com.kalibyte.tradex.trading.entity.*;
import com.kalibyte.tradex.trading.repository.*;
import com.kalibyte.tradex.party.entity.Party;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;
import com.kalibyte.tradex.notification.event.TradexNotificationEvents;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * InventoryLockEngine - Manages pessimistic locking for inventory management
 * 
 * Ensures atomic operations on supply inventory when deals are locked.
 * Uses REPEATABLE_READ isolation to prevent phantom reads.
 * 
 * When a deal is locked:
 * 1. Supply row is locked with SELECT...FOR UPDATE
 * 2. Available quantity is decremented
 * 3. Locked quantity is incremented
 * 4. Contract is automatically generated
 * 5. Deal status transitions to LOCKED
 * 6. Inventory is reserved for this deal
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class InventoryLockEngine {
    
    private final SupplyRepository supplyRepository;
    private final DealRepository dealRepository;
    private final ContractRepository contractRepository;
    private final WhatsAppService whatsAppService;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Lock a deal and issue contract
     * 
     * This operation is atomic and prevents concurrent modifications.
     * The pessimistic lock ensures no other transaction can modify the supply
     * until this transaction commits.
     * 
     * @param dealId Deal to lock
     * @param operatorName Who performed the lock
     * @return Updated deal with lock information
     */
    public DealDTO lockDealAndIssueContract(Long dealId, String operatorName) {
        log.info("Starting deal lock process for deal: {}", dealId);
        
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        
        if ("LOCKED".equals(deal.getLockStatus())) {
            log.warn("Deal {} is already locked", dealId);
            throw new IllegalStateException("Deal is already locked");
        }
        
        // CRITICAL: Lock the supply row for exclusive write access
        // This prevents other threads from modifying this supply until transaction commits
        Supply supply = supplyRepository.findByIdWithLock(deal.getSupply().getId())
                .orElseThrow(() -> new IllegalArgumentException("Supply not found (locked lookup failed)"));
        
        log.debug("Supply locked for deal {}: Available: {}, Locked: {}", 
                dealId, supply.getAvailableQuantity(), supply.getLockedQuantity());
        
        // Verify sufficient inventory is available
        if (supply.getAvailableQuantity().compareTo(deal.getQuantity()) < 0) {
            log.error("Insufficient inventory. Available: {}, Required: {}", 
                    supply.getAvailableQuantity(), deal.getQuantity());
            throw new IllegalStateException("Insufficient inventory available for locking");
        }
        
        // Atomically update inventory
        supply.setAvailableQuantity(
                supply.getAvailableQuantity().subtract(deal.getQuantity())
        );
        supply.setLockedQuantity(
                supply.getLockedQuantity().add(deal.getQuantity())
        );
        
        // Update supply status based on remaining inventory
        if (supply.getAvailableQuantity().compareTo(BigDecimal.ZERO) == 0) {
            supply.setStatus("SOLD_OUT");
        } else {
            supply.setStatus("PARTIAL");
        }
        
        supply.setUpdatedAt(LocalDateTime.now());
        supplyRepository.save(supply);
        
        log.info("Inventory locked. New available: {}, New locked: {}", 
                supply.getAvailableQuantity(), supply.getLockedQuantity());
        
        // Generate contract automatically
        Contract contract = generateContract(deal, supply);
        contract = contractRepository.save(contract);
        
        log.info("Contract generated: {}", contract.getContractNumber());
        
        // Update deal lock status
        deal.setLockStatus("LOCKED");
        deal.setStatus("LOCKED");
        deal.setLockedAt(LocalDateTime.now());
        deal.setLockedBy(operatorName);
        deal.setUpdatedAt(LocalDateTime.now());
        
        Deal lockedDeal = dealRepository.save(deal);
        
        // Publish async WhatsApp event: 1. Quantity Locked & 2. Contract Ready
        try {
            String dealIdentifier = deal.getDealNumber() != null ? deal.getDealNumber() : "DEAL-" + deal.getId();
            String commodityName = supply.getCommodity().getCommodityName();
            String unit = supply.getCommodity().getDefaultUnit() != null ? supply.getCommodity().getDefaultUnit() : "MT";
            
            TradexTemplateDtos.QuantityLockedTemplate lockTemplate = TradexTemplateDtos.QuantityLockedTemplate.builder()
                    .dealId(dealIdentifier)
                    .commodity(commodityName)
                    .lockedQuantity(deal.getQuantity().toPlainString())
                    .unit(unit)
                    .remainingQuantity(supply.getAvailableQuantity().toPlainString())
                    .transactionLink("https://kkcanvassing.com/party-login")
                    .build();
            
            Party buyer = deal.getBuyerParty();
            Party seller = deal.getSellerParty();

            String sellerLoc = seller != null ? 
                    ((seller.getCity() != null ? seller.getCity() + ", " : "") + (seller.getState() != null ? seller.getState() : "")) : "Direct Plant";
            String buyerLoc = buyer != null ? 
                    ((buyer.getCity() != null ? buyer.getCity() + ", " : "") + (buyer.getState() != null ? buyer.getState() : "")) : "Direct Delivery";

            TradexTemplateDtos.ContractReadyTemplate buyerContractTemplate = TradexTemplateDtos.ContractReadyTemplate.builder()
                    .contractNumber(contract.getContractNumber())
                    .commodity(commodityName)
                    .quantity(deal.getQuantity().toPlainString())
                    .unit(unit)
                    .finalRate(deal.getRate().toPlainString())
                    .contractLink("https://kkcanvassing.com/party-login")
                    .counterpartyRole("Seller")
                    .counterpartyName(seller != null ? seller.getCompanyName() : "Verified Seller")
                    .counterpartyPhone(seller != null ? seller.getPhone() : "-")
                    .counterpartyLocation(sellerLoc)
                    .build();

            TradexTemplateDtos.ContractReadyTemplate sellerContractTemplate = TradexTemplateDtos.ContractReadyTemplate.builder()
                    .contractNumber(contract.getContractNumber())
                    .commodity(commodityName)
                    .quantity(deal.getQuantity().toPlainString())
                    .unit(unit)
                    .finalRate(deal.getRate().toPlainString())
                    .contractLink("https://kkcanvassing.com/party-login")
                    .counterpartyRole("Buyer")
                    .counterpartyName(buyer != null ? buyer.getCompanyName() : "Verified Buyer")
                    .counterpartyPhone(buyer != null ? buyer.getPhone() : "-")
                    .counterpartyLocation(buyerLoc)
                    .build();
            
            String buyerPhone = (buyer != null) ? buyer.getPhone() : null;
            String sellerPhone = (seller != null) ? seller.getPhone() : null;

            eventPublisher.publishEvent(TradexNotificationEvents.DealLockedEvent.builder()
                    .buyerPhone(buyerPhone)
                    .sellerPhone(sellerPhone)
                    .lockTemplate(lockTemplate)
                    .contractTemplate(buyerContractTemplate)
                    .buyerContractTemplate(buyerContractTemplate)
                    .sellerContractTemplate(sellerContractTemplate)
                    .build());
            log.info("Published async WhatsApp quantity locked & contract ready events for deal {}", dealIdentifier);
        } catch (Exception e) {
            log.error("Failed to publish async WhatsApp notifications for locked deal {}: {}", deal.getId(), e.getMessage());
        }
        
        log.info("Deal locked successfully. Deal: {}, Status: {}, Locked By: {}", 
                dealId, deal.getStatus(), operatorName);
        
        return DealDTO.fromEntity(lockedDeal);
    }
    
    /**
     * Generate contract document automatically
     */
    private Contract generateContract(Deal deal, Supply supply) {
        String contractNumber = generateContractNumber(deal);
        
        // Build terms and conditions as JSONB
        String termsJson = buildTermsJson(deal, supply);
        
        Contract contract = Contract.builder()
                .deal(deal)
                .sellerName(deal.getSellerParty().getCompanyName())
                .sellerGstin(deal.getSellerParty().getGstin())
                .sellerAddress(deal.getSellerParty().getAddress())
                .buyerName(deal.getBuyerParty().getCompanyName())
                .buyerGstin(deal.getBuyerParty().getGstin())
                .buyerAddress(deal.getBuyerParty().getAddress())
                .contractNumber(contractNumber)
                .commodityName(supply.getCommodity().getCommodityName())
                .quantity(deal.getQuantity())
                .rate(deal.getRate())
                .termsConditions(termsJson)
                .deliveryZone(supply.getOriginZone())
                .status("ISSUED")
                .issuedAt(LocalDateTime.now())
                .createdByPartyId(deal.getSellerParty().getId())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return contract;
    }
    
    /**
     * Generate unique contract number
     */
    private String generateContractNumber(Deal deal) {
        // Format: TRX-2026-{dealId}-{timestamp}
        return String.format("TRX-2026-%d-%d", 
                deal.getId(), 
                System.currentTimeMillis() % 10000);
    }
    
    /**
     * Build terms and conditions JSON
     */
    private String buildTermsJson(Deal deal, Supply supply) {
        // Simplified T&C - in production, this would be more comprehensive
        return String.format(
                "{\"paymentTerms\": \"As per invoice\", " +
                "\"deliveryTerms\": \"As agreed\", " +
                "\"dispatchBy\": \"%s\", " +
                "\"qualityCertificate\": \"Required\", " +
                "\"inspectionClause\": \"7 days after delivery\"}",
                supply.getValidTill()
        );
    }
}
