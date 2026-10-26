package com.kalibyte.tradex.commodity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddQualityGradeRequest {
    
    private String gradeName;
    
    private String gradeCode;
    
    private String description;
}
