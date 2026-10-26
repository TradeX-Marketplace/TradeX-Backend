package com.kalibyte.tradex.commodity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityGradeDTO {
    
    private Long id;
    
    private String gradeName;
    
    private String gradeCode;
    
    private String description;
    
    private Boolean active;
}
