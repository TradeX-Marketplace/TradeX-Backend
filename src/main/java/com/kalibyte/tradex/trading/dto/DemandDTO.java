package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Demand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandDTO {
    
    private Long id;
    private String demandNumber;
    private String commodityName;
    private String qualityGrade;
    private BigDecimal quantity;
    private BigDecimal fulfilledQuantity;
    private BigDecimal targetRate;
    private String destination;
    private String destinationZone;
    private String maskedLabel;
    private LocalDate validTill;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static DemandDTO fromEntity(Demand demand) {
        return DemandDTO.builder()
                .id(demand.getId())
                .demandNumber(demand.getDemandNumber())
                .commodityName(demand.getCommodity().getCommodityName())
                .qualityGrade(demand.getQualityGrade() != null ? demand.getQualityGrade().getGradeName() : null)
                .quantity(demand.getRequiredQuantity())
                .fulfilledQuantity(demand.getFulfilledQuantity())
                .targetRate(demand.getTargetRate())
                .destination(demand.getDestination())
                .destinationZone(demand.getDestinationZone())
                .maskedLabel(demand.getMaskedLabel())
                .validTill(demand.getValidTill())
                .status(demand.getStatus())
                .createdBy(demand.getCreatedBy())
                .createdAt(demand.getCreatedAt())
                .build();
    }
}
