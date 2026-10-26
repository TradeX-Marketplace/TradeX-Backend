package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.NegotiationRound;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegotiationRoundDTO {
    
    private Integer roundNumber;
    private String offeredBy;
    private String offeredByPartyId;
    private BigDecimal rate;
    private String remarks;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static NegotiationRoundDTO fromEntity(NegotiationRound round) {
        return NegotiationRoundDTO.builder()
                .roundNumber(round.getRoundNumber())
                .offeredBy(round.getOfferedBy())
                .rate(round.getRate())
                .remarks(round.getRemarks())
                .createdAt(round.getCreatedAt())
                .build();
    }
}
