package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Deal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealDTO {
    
    private Long id;
    private String dealNumber;
    private String commodityName;
    private String buyerName;
    private String sellerName;
    private String buyerPhone;
    private String buyerLocation;
    private String sellerPhone;
    private String sellerLocation;
    private BigDecimal quantity;
    private BigDecimal finalRate;
    private BigDecimal totalValue;
    private String destination;
    private LocalDate dealDate;
    private String status;
    private String lockStatus;
    private LocalDateTime lockedAt;
    private String lockedBy;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static DealDTO fromEntity(Deal deal) {
        boolean isLocked = "LOCKED".equalsIgnoreCase(deal.getLockStatus()) 
                || "LOCKED".equalsIgnoreCase(deal.getStatus())
                || "CONTRACT_ISSUED".equalsIgnoreCase(deal.getStatus())
                || "APPROVED".equalsIgnoreCase(deal.getStatus());
        
        String buyerPhone = null;
        String buyerLoc = null;
        String sellerPhone = null;
        String sellerLoc = null;
        
        if (isLocked) {
            if (deal.getBuyerParty() != null) {
                buyerPhone = deal.getBuyerParty().getPhone();
                buyerLoc = (deal.getBuyerParty().getCity() != null ? deal.getBuyerParty().getCity() + ", " : "") +
                           (deal.getBuyerParty().getState() != null ? deal.getBuyerParty().getState() : "");
            }
            if (deal.getSellerParty() != null) {
                sellerPhone = deal.getSellerParty().getPhone();
                sellerLoc = (deal.getSellerParty().getCity() != null ? deal.getSellerParty().getCity() + ", " : "") +
                            (deal.getSellerParty().getState() != null ? deal.getSellerParty().getState() : "");
            }
        }

        return DealDTO.builder()
                .id(deal.getId())
                .dealNumber(deal.getDealNumber())
                .commodityName(deal.getSupply() != null && deal.getSupply().getCommodity() != null 
                        ? deal.getSupply().getCommodity().getCommodityName() : "")
                .buyerName(deal.getBuyerParty() != null ? deal.getBuyerParty().getCompanyName() : "")
                .sellerName(deal.getSellerParty() != null ? deal.getSellerParty().getCompanyName() : "")
                .buyerPhone(buyerPhone)
                .buyerLocation(buyerLoc)
                .sellerPhone(sellerPhone)
                .sellerLocation(sellerLoc)
                .quantity(deal.getQuantity())
                .finalRate(deal.getRate())
                .totalValue(deal.getTotalValue())
                .destination(deal.getSupply() != null ? deal.getSupply().getLocation() : "")
                .dealDate(deal.getCreatedAt() != null ? deal.getCreatedAt().toLocalDate() : null)
                .status(deal.getStatus())
                .lockStatus(deal.getLockStatus())
                .lockedAt(deal.getLockedAt())
                .lockedBy(deal.getLockedBy())
                .createdAt(deal.getCreatedAt())
                .build();
    }
}
