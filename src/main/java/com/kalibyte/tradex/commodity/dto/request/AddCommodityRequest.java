package com.kalibyte.tradex.commodity.dto.request;

import com.kalibyte.tradex.commodity.entity.Commodity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommodityRequest {
    
    private String commodityName; // Required
    
    private String parentName; // Optional (defaults to commodityName)

    private String qualityName; // Optional (quality variance)
    
    private String category; // Required
    
    private String subCategory; // Optional
    
    private String defaultUnit; // Required (MT / KG / Ton)
    
    private String description;
    
    private Commodity.CommodityStatus status; // Default: ACTIVE
    
    private Set<AddQualityGradeRequest> qualityGrades; // Optional
}
