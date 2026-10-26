package com.kalibyte.tradex.commodity.dto;

import com.kalibyte.tradex.commodity.entity.Commodity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommodityDTO {
    
    private Long id;
    
    private String commodityCode;
    
    private String commodityName;
    
    private String parentName;

    private String qualityName;
    
    private String category;
    
    private String subCategory;
    
    private String defaultUnit;
    
    private String description;
    
    private Commodity.CommodityStatus status;
    
    private Set<QualityGradeDTO> qualityGrades;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private String updatedBy;
    
    private LocalDateTime updatedAt;
}
