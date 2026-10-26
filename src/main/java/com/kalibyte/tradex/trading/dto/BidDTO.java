package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidDTO {
    
    private Long id;
    private String bidNumber;
    private String commodityName;
    private String maskedBidderId;
    private BigDecimal marketRate;
    private BigDecimal bidRate;
    private BigDecimal counterRate;
    private BigDecimal quantity;
    private String destination;
    private String bidType;
    private String remarks;
    private String counterRemarks;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert entity to DTO
     */
    public static BidDTO fromEntity(Bid bid) {
        return BidDTO.builder()
                .id(bid.getId())
                .bidNumber(bid.getBidNumber())
                .commodityName(bid.getCommodity().getCommodityName())
                .maskedBidderId("BDR-" + (bid.getParty().getId() % 10000))
                .bidRate(bid.getBidRate())
                .counterRate(bid.getCounterRate())
                .quantity(bid.getQuantity())
                .destination(bid.getDestination())
                .bidType(bid.getBidType())
                .remarks(bid.getRemarks())
                .counterRemarks(bid.getCounterRemarks())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }
}
