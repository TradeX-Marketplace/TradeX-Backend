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
public class PlaceBidRequest {
    
    private Long commodityId;
    private Long supplyId;
    private Long demandId;
    private String bidType; // BUY or SELL
    private BigDecimal bidRate;
    private BigDecimal quantity;
    private String destination;
    private String remarks;
}
