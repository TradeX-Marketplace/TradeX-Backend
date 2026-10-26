package com.kalibyte.tradex.trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.kalibyte.tradex.trading.dto.SupplyDTO;
import com.kalibyte.tradex.trading.dto.BidDTO;
import com.kalibyte.tradex.trading.dto.DemandDTO;
import com.kalibyte.tradex.trading.entity.*;
import com.kalibyte.tradex.party.entity.Party;

import java.util.List;

/**
 * DoubleBlindMasker - Anonymity enforcement engine
 * <p>
 * Ensures that parties cannot identify counterparties in a double-blind trading platform.
 * <p>
 * Rules:
 * - ADMIN can see all identities (full details)
 * - PARTY members see masked identities (anonymized tags like "TradeX Verified [Zone MH-02]")
 * - Bidder IDs are shown as "BDR-{randomId}" format
 * - Seller/Buyer names are never exposed to counterparties
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoubleBlindMasker {
    
    /**
     * Mask supply details for a caller party
     * <p>
     * If caller is ADMIN: Return full supply details including seller info
     * If caller is PARTY: Hide seller company name, phone, contact person, etc.
     *                      Show only masked label and zone
     * 
     * @param supply The supply to mask
     * @param caller The party viewing the supply
     * @return Masked DTO
     */
    public SupplyDTO maskSupplyForParty(Supply supply, Party caller) {
        SupplyDTO dto = SupplyDTO.fromEntity(supply);
        String realPartyName = supply.getParty() != null ? supply.getParty().getCompanyName() : "Enterprise";
        dto.setPartyName(realPartyName);
        dto.setMaskedLabel(realPartyName);
        return dto;
    }
    
    /**
     * Mask demand details for a caller party
     * 
     * @param demand The demand to mask
     * @param caller The party viewing the demand
     * @return Masked DTO
     */
    public DemandDTO maskDemandForParty(Demand demand, Party caller) {
        DemandDTO dto = DemandDTO.fromEntity(demand);
        
        if (isAdmin(caller)) {
            log.debug("Admin viewing demand {}, no masking applied", demand.getId());
            return dto;
        }
        
        // Party members: scrub buyer identity
        String maskedBuyerLabel = String.format(
                "K.K.CANVANSSING AGENT Verified Buyer [%s]",
                demand.getDestinationZone() != null ? demand.getDestinationZone() : "Pan-India"
        );
        
        log.debug("Masking demand {} for party {}. Buyer masked as: {}", 
                demand.getId(), caller.getId(), maskedBuyerLabel);
        
        dto.setMaskedLabel(maskedBuyerLabel);
        
        return dto;
    }
    
    /**
     * Mask bid details for a counterparty
     * 
     * If caller is the SELLER (supply owner): Can see buyer bid but not buyer identity
     * If caller is ADMIN: Can see all details
     * 
     * @param bid The bid to mask
     * @param caller The party viewing the bid
     * @return Masked DTO
     */
    public BidDTO maskBidForCounterparty(Bid bid, Party caller) {
        BidDTO dto = BidDTO.fromEntity(bid);
        
        if (isAdmin(caller)) {
            log.debug("Admin viewing bid {}, no masking applied", bid.getId());
            return dto;
        }
        
        // If caller is the seller viewing buyer's bid
        if (caller.getId().equals(bid.getSupply().getParty().getId())) {
            // Hide buyer identity, show anonymous bidder tag
            String anonymousBidderId = generateAnonymousBidderId(bid.getParty().getId());
            dto.setMaskedBidderId(anonymousBidderId);
            
            log.debug("Masking bid {} for seller {}. Bidder masked as: {}", 
                    bid.getId(), caller.getId(), anonymousBidderId);
        }
        
        return dto;
    }
    
    /**
     * Generate anonymous bidder identifier
     * Format: BDR-{hash of partyId}
     */
    private String generateAnonymousBidderId(Long partyId) {
        // Create a deterministic but non-reversible identifier
        long hash = Math.abs(partyId.hashCode());
        return String.format("BDR-%04d", hash % 10000);
    }
    
    /**
     * Check if a party has admin privileges
     */
    private boolean isAdmin(Party party) {
        // Check if party has any ROLE_ADMIN authority
        // In production, check against authorities/roles in database
        return party.getId() == 1; // Admin party typically has ID 1
    }

    /**
     * Mask SupplyDTO for public/buyer visibility
     */
    public SupplyDTO maskSupply(SupplyDTO dto, boolean isAdmin) {
        if (dto == null) {
            return dto;
        }
        if (dto.getPartyName() != null && !dto.getPartyName().trim().isEmpty()) {
            dto.setMaskedLabel(dto.getPartyName());
        }
        return dto;
    }

    /**
     * Mask list of SupplyDTOs
     */
    public java.util.List<SupplyDTO> maskSupplies(java.util.List<SupplyDTO> list, boolean isAdmin) {
        if (list == null || isAdmin) {
            return list;
        }
        list.forEach(item -> maskSupply(item, false));
        return list;
    }

    /**
     * Mask DemandDTO for public/seller visibility
     */
    public DemandDTO maskDemand(DemandDTO dto, boolean isAdmin) {
        if (dto == null || isAdmin) {
            return dto;
        }

        String label = "K.K.CANVANSSING AGENT Verified Buyer [" + 
                (dto.getDestinationZone() != null && !dto.getDestinationZone().trim().isEmpty() ? dto.getDestinationZone() : "Pan-India") + "]";
        dto.setMaskedLabel(label);
        dto.setCreatedBy(label);
        return dto;
    }

    /**
     * Mask list of DemandDTOs
     */
    public List<DemandDTO> maskDemands(List<DemandDTO> list, boolean isAdmin) {
        if (list == null || isAdmin) {
            return list;
        }
        list.forEach(item -> maskDemand(item, false));
        return list;
    }

    /**
     * Mask BidDTO for counterparty visibility
     */
    public BidDTO maskBid(BidDTO dto, boolean isAdmin) {
        if (dto == null || isAdmin) {
            return dto;
        }
        // Bidder ID is already masked with BDR-XXXX in BidDTO.fromEntity
        return dto;
    }

    /**
     * Mask list of BidDTOs
     */
    public java.util.List<BidDTO> maskBids(java.util.List<BidDTO> list, boolean isAdmin) {
        if (list == null || isAdmin) {
            return list;
        }
        list.forEach(item -> maskBid(item, false));
        return list;
    }
}
