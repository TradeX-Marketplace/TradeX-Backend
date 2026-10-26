package com.kalibyte.tradex.trading.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDemandRequest {
    
    private Long commodityId;
    private Long qualityGradeId;
    private BigDecimal quantity;
    private BigDecimal targetRate;
    private String destination;
    private String destinationZone;
    private LocalDate validTill;
}
