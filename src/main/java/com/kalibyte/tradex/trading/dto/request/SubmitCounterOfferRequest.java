package com.kalibyte.tradex.trading.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitCounterOfferRequest {
    
    private BigDecimal counterRate;
    private String remarks;
}
