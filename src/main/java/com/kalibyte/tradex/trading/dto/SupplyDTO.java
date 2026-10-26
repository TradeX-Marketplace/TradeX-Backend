package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Supply;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyDTO {
    
    private Long id;
    private String supplyNumber;
    private Long partyId;
    private String partyName;
    private String commodityName;
    private String parentCommodityName;
    private String qualityName;
    private String qualityGrade;
    private BigDecimal totalQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal lockedQuantity;
    private BigDecimal askingRate;
    private String location;
    private String originZone;
    private Long originManufacturerId;
    private String originManufacturerName;
    private String maskedLabel;
    private String labSpecs;
    private LocalDate validTill;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static SupplyDTO fromEntity(Supply supply) {
        String parentComm = supply.getCommodity() != null
                ? (supply.getCommodity().getParentName() != null ? supply.getCommodity().getParentName() : supply.getCommodity().getCommodityName())
                : null;
        String qName = supply.getQualityGrade() != null
                ? supply.getQualityGrade().getGradeName()
                : (supply.getCommodity() != null ? supply.getCommodity().getQualityName() : null);
        Long pId = supply.getParty() != null ? supply.getParty().getId() : null;
        String pName = supply.getParty() != null ? supply.getParty().getCompanyName() : null;

        return SupplyDTO.builder()
                .id(supply.getId())
                .supplyNumber(supply.getSupplyNumber())
                .partyId(pId)
                .partyName(pName)
                .commodityName(supply.getCommodity().getCommodityName())
                .parentCommodityName(parentComm)
                .qualityName(qName)
                .qualityGrade(qName)
                .totalQuantity(supply.getTotalQuantity())
                .availableQuantity(supply.getAvailableQuantity())
                .lockedQuantity(supply.getLockedQuantity())
                .askingRate(supply.getAskingRate())
                .location(supply.getLocation())
                .originZone(supply.getOriginZone())
                .originManufacturerId(supply.getOriginManufacturerId())
                .originManufacturerName(supply.getOriginManufacturerName())
                .maskedLabel(pName != null ? pName : supply.getMaskedLabel())
                .labSpecs(supply.getLabSpecs())
                .validTill(supply.getValidTill())
                .status(supply.getStatus())
                .createdBy(supply.getCreatedBy())
                .createdAt(supply.getCreatedAt())
                .build();
    }
}
