package com.kalibyte.tradex.commodity.service;

import com.kalibyte.tradex.commodity.dto.CommodityDTO;
import com.kalibyte.tradex.commodity.dto.QualityGradeDTO;
import com.kalibyte.tradex.commodity.dto.request.AddCommodityRequest;
import com.kalibyte.tradex.commodity.dto.request.AddQualityGradeRequest;
import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.commodity.entity.QualityGrade;
import com.kalibyte.tradex.commodity.repository.CommodityRepository;
import com.kalibyte.tradex.commodity.repository.QualityGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommodityService {
    
    private final CommodityRepository commodityRepository;
    private final QualityGradeRepository qualityGradeRepository;
    
    /**
     * Add new commodity or quality variance
     * Admin only operation
     */
    @Transactional
    public CommodityDTO addCommodity(AddCommodityRequest request, String createdBy) {
        // Validate request
        validateCommodityRequest(request);
        
        String parentName = request.getParentName() != null && !request.getParentName().trim().isEmpty()
                ? request.getParentName().trim().toUpperCase()
                : request.getCommodityName().trim().toUpperCase();

        String qualityName = request.getQualityName() != null && !request.getQualityName().trim().isEmpty()
                ? request.getQualityName().trim().toUpperCase()
                : null;

        String commodityName = request.getCommodityName().trim();
        if (qualityName != null && !qualityName.isEmpty()) {
            if (commodityRepository.existsByParentNameAndQualityName(parentName, qualityName)) {
                throw new RuntimeException("Commodity with quality variance '" + qualityName + "' already exists under " + parentName);
            }
            if (commodityName.equalsIgnoreCase(parentName)) {
                commodityName = parentName + " (" + qualityName + ")";
            }
        } else {
            if (commodityRepository.existsByCommodityName(commodityName)) {
                throw new RuntimeException("Commodity with this name already exists");
            }
        }
        
        // Generate commodity code
        String commodityCode = generateCommodityCode();
        
        // Create commodity
        Commodity commodity = Commodity.builder()
                .commodityCode(commodityCode)
                .commodityName(commodityName)
                .parentName(parentName)
                .qualityName(qualityName)
                .category(request.getCategory() != null ? request.getCategory() : "Agricultural Commodity")
                .subCategory(request.getSubCategory())
                .defaultUnit(request.getDefaultUnit() != null ? request.getDefaultUnit() : "MT")
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Commodity.CommodityStatus.ACTIVE)
                .createdBy(createdBy)
                .build();
        
        // Save commodity first
        Commodity savedCommodity = commodityRepository.save(commodity);
        
        // Add quality grades if supplied
        if (request.getQualityGrades() != null && !request.getQualityGrades().isEmpty()) {
            Set<QualityGrade> grades = new HashSet<>();
            for (AddQualityGradeRequest gradeRequest : request.getQualityGrades()) {
                QualityGrade grade = QualityGrade.builder()
                        .commodity(savedCommodity)
                        .gradeName(gradeRequest.getGradeName())
                        .gradeCode(gradeRequest.getGradeCode())
                        .description(gradeRequest.getDescription())
                        .active(true)
                        .build();
                grades.add(qualityGradeRepository.save(grade));
            }
            savedCommodity.setQualityGrades(grades);
        } else if (qualityName != null && !qualityName.isEmpty()) {
            QualityGrade grade = QualityGrade.builder()
                    .commodity(savedCommodity)
                    .gradeName(qualityName)
                    .gradeCode(qualityName.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(qualityName.length(), 4)).toUpperCase())
                    .description(parentName + " - " + qualityName)
                    .active(true)
                    .build();
            QualityGrade savedGrade = qualityGradeRepository.save(grade);
            Set<QualityGrade> grades = new HashSet<>();
            grades.add(savedGrade);
            savedCommodity.setQualityGrades(grades);
        }
        
        // Save commodity with relationships
        Commodity finalCommodity = commodityRepository.save(savedCommodity);
        return convertToDTO(finalCommodity);
    }
    
    /**
     * Get commodity by ID
     */
    public CommodityDTO getCommodityById(Long commodityId) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("Commodity not found with ID: " + commodityId));
        return convertToDTO(commodity);
    }
    
    /**
     * Get commodity by commodity code
     */
    public CommodityDTO getCommodityByCode(String commodityCode) {
        Commodity commodity = commodityRepository.findByCommodityCode(commodityCode)
                .orElseThrow(() -> new RuntimeException("Commodity not found with code: " + commodityCode));
        return convertToDTO(commodity);
    }
    
    /**
     * Get all commodities with pagination
     */
    public Page<CommodityDTO> getAllCommodities(Pageable pageable) {
        return commodityRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Update commodity
     * Admin only operation
     */
    @Transactional
    public CommodityDTO updateCommodity(Long commodityId, AddCommodityRequest request, String updatedBy) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("Commodity not found with ID: " + commodityId));
        
        // Check if name is being changed to an existing commodity
        if (!commodity.getCommodityName().equals(request.getCommodityName()) &&
                commodityRepository.existsByCommodityName(request.getCommodityName())) {
            throw new RuntimeException("Commodity with this name already exists");
        }
        
        // Update basic fields
        commodity.setCommodityName(request.getCommodityName());
        if (request.getParentName() != null && !request.getParentName().trim().isEmpty()) {
            commodity.setParentName(request.getParentName().trim());
        }
        if (request.getQualityName() != null) {
            commodity.setQualityName(request.getQualityName().trim());
        }
        commodity.setCategory(request.getCategory() != null && !request.getCategory().trim().isEmpty() 
                ? request.getCategory() 
                : (commodity.getCategory() != null ? commodity.getCategory() : "General"));
        commodity.setSubCategory(request.getSubCategory());
        commodity.setDefaultUnit(request.getDefaultUnit() != null ? request.getDefaultUnit() : "MT");
        commodity.setDescription(request.getDescription());
        commodity.setStatus(request.getStatus() != null ? request.getStatus() : Commodity.CommodityStatus.ACTIVE);
        commodity.setUpdatedBy(updatedBy);
        
        Commodity updatedCommodity = commodityRepository.save(commodity);
        return convertToDTO(updatedCommodity);
    }
    
    /**
     * Add quality grade to existing commodity
     */
    @Transactional
    public QualityGradeDTO addQualityGrade(Long commodityId, AddQualityGradeRequest request) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("Commodity not found with ID: " + commodityId));
        
        // Check for duplicate grade code
        if (qualityGradeRepository.existsByCommodityIdAndGradeCode(commodityId, request.getGradeCode())) {
            throw new RuntimeException("Quality grade with this code already exists for this commodity");
        }
        
        QualityGrade grade = QualityGrade.builder()
                .commodity(commodity)
                .gradeName(request.getGradeName())
                .gradeCode(request.getGradeCode())
                .description(request.getDescription())
                .active(true)
                .build();
        
        QualityGrade savedGrade = qualityGradeRepository.save(grade);
        commodity.getQualityGrades().add(savedGrade);
        commodityRepository.save(commodity);
        
        return convertQualityGradeToDTO(savedGrade);
    }
    
    /**
     * Deactivate commodity
     */
    @Transactional
    public CommodityDTO deactivateCommodity(Long commodityId, String updatedBy) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("Commodity not found with ID: " + commodityId));
        
        commodity.setStatus(Commodity.CommodityStatus.INACTIVE);
        commodity.setUpdatedBy(updatedBy);
        
        Commodity updatedCommodity = commodityRepository.save(commodity);
        return convertToDTO(updatedCommodity);
    }
    
    /**
     * Delete / remove commodity from master
     */
    @Transactional
    public void deleteCommodity(Long commodityId, String updatedBy) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("Commodity not found with ID: " + commodityId));
        commodityRepository.delete(commodity);
    }
    
    /**
     * Validate commodity request
     */
    private void validateCommodityRequest(AddCommodityRequest request) {
        if (request.getCommodityName() == null || request.getCommodityName().isEmpty()) {
            throw new RuntimeException("Commodity name is required");
        }
        
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            request.setCategory("Agricultural Commodity");
        }
        
        if (request.getDefaultUnit() == null || request.getDefaultUnit().isEmpty()) {
            request.setDefaultUnit("MT");
        }
    }
    
    /**
     * Generate unique commodity code
     * Format: COM-XXXX (short format)
     * Example: COM-0001, COM-0002, ..., COM-9999
     */
    private String generateCommodityCode() {
        try {
            long sequence = commodityRepository.countAllCommodities() + 1;
            String commodityCode = String.format("COM-%04d", sequence % 10000);
            
            int retryCount = 0;
            while (commodityRepository.existsByCommodityCode(commodityCode) && retryCount < 5) {
                sequence++;
                commodityCode = String.format("COM-%04d", sequence % 10000);
                retryCount++;
            }
            
            return commodityCode;
        } catch (Exception e) {
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            return String.format("COM-%s", uuid);
        }
    }
    
    /**
     * Convert Commodity entity to CommodityDTO
     */
    public CommodityDTO convertToDTO(Commodity commodity) {
        return CommodityDTO.builder()
                .id(commodity.getId())
                .commodityCode(commodity.getCommodityCode())
                .commodityName(commodity.getCommodityName())
                .parentName(commodity.getParentName() != null ? commodity.getParentName() : commodity.getCommodityName())
                .qualityName(commodity.getQualityName())
                .category(commodity.getCategory())
                .subCategory(commodity.getSubCategory())
                .defaultUnit(commodity.getDefaultUnit())
                .description(commodity.getDescription())
                .status(commodity.getStatus())
                .qualityGrades(commodity.getQualityGrades().stream()
                        .map(this::convertQualityGradeToDTO)
                        .collect(Collectors.toSet()))
                .createdBy(commodity.getCreatedBy())
                .createdAt(commodity.getCreatedAt())
                .updatedBy(commodity.getUpdatedBy())
                .updatedAt(commodity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert QualityGrade entity to QualityGradeDTO
     */
    public QualityGradeDTO convertQualityGradeToDTO(QualityGrade grade) {
        return QualityGradeDTO.builder()
                .id(grade.getId())
                .gradeName(grade.getGradeName())
                .gradeCode(grade.getGradeCode())
                .description(grade.getDescription())
                .active(grade.getActive())
                .build();
    }
}
