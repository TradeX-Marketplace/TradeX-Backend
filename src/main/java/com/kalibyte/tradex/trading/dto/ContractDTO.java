package com.kalibyte.tradex.trading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalibyte.tradex.trading.entity.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDTO {
    
    private Long id;
    private String contractNumber;
    private String commodityName;
    private String buyerName;
    private String sellerName;
    private String buyerGstin;
    private String sellerGstin;
    private String buyerAddress;
    private String sellerAddress;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal finalRate;
    private BigDecimal totalValue;
    private String deliveryTerms;
    private String paymentTerms;
    private String destination;
    private LocalDate contractDate;
    private LocalDate dispatchDeadline;
    private String status;
    private LocalDateTime signedAt;
    private Map<String, Object> termsAndConditions;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static ContractDTO fromEntity(Contract contract) {
        return ContractDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .commodityName(contract.getCommodityName())
                .buyerName(contract.getBuyerName())
                .sellerName(contract.getSellerName())
                .buyerGstin(contract.getBuyerGstin())
                .sellerGstin(contract.getSellerGstin())
                .buyerAddress(contract.getBuyerAddress())
                .sellerAddress(contract.getSellerAddress())
                .quantity(contract.getQuantity())
                .finalRate(contract.getRate())
                .totalValue(contract.getTotalAmount())
                .destination(contract.getDeliveryZone())
                .contractDate(contract.getIssuedAt().toLocalDate())
                .dispatchDeadline(contract.getDispatchDeadline() != null ? contract.getDispatchDeadline().toLocalDate() : null)
                .status(contract.getStatus())
                .createdAt(contract.getIssuedAt())
                .build();
    }
}
