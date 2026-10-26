package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Negotiation;
import com.kalibyte.tradex.trading.entity.NegotiationRound;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationDTO {
    
    private Long id;
    private String negotiationCode;
    private String commodityName;
    private BigDecimal quantity;
    private String destination;
    private BigDecimal marketRate;
    private BigDecimal buyerOffer;
    private BigDecimal sellerOffer;
    private BigDecimal currentOffer;
    private String lastOfferBy;
    private String status;
    private List<NegotiationRoundDTO> rounds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert entity to DTO with rounds
     */
    public static NegotiationDTO fromEntity(Negotiation negotiation, List<NegotiationRound> rounds) {
        List<NegotiationRoundDTO> roundDTOs = rounds.stream()
                .map(NegotiationRoundDTO::fromEntity)
                .collect(Collectors.toList());
        
        return NegotiationDTO.builder()
                .id(negotiation.getId())
                .negotiationCode(negotiation.getNegotiationCode())
                .commodityName(negotiation.getCommodity().getCommodityName())
                .quantity(negotiation.getQuantity())
                .destination(negotiation.getDestination())
                .currentOffer(negotiation.getCurrentOffer())
                .buyerOffer(negotiation.getBuyerOffer())
                .sellerOffer(negotiation.getSellerOffer())
                .lastOfferBy(negotiation.getLastOfferBy())
                .status(negotiation.getStatus())
                .rounds(roundDTOs)
                .createdAt(negotiation.getCreatedAt())
                .updatedAt(negotiation.getUpdatedAt())
                .build();
    }
}
