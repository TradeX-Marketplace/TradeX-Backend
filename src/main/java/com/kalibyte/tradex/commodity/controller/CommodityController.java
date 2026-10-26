package com.kalibyte.tradex.commodity.controller;

import com.kalibyte.tradex.commodity.dto.CommodityDTO;
import com.kalibyte.tradex.commodity.dto.QualityGradeDTO;
import com.kalibyte.tradex.commodity.dto.request.AddCommodityRequest;
import com.kalibyte.tradex.commodity.dto.request.AddQualityGradeRequest;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.commodity.service.CommodityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commodities")
@RequiredArgsConstructor
public class CommodityController {
    
    private final CommodityService commodityService;
    
    /**
     * Add new commodity
     * Admin only operation
     * POST /api/v1/commodities
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommodityDTO>> addCommodity(
            @RequestBody AddCommodityRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            CommodityDTO commodityDTO = commodityService.addCommodity(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(commodityDTO, "Commodity created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get commodity by ID
     * GET /api/v1/commodities/{commodityId}
     */
    @GetMapping("/{commodityId}")
    public ResponseEntity<ApiResponse<CommodityDTO>> getCommodityById(@PathVariable Long commodityId) {
        try {
            CommodityDTO commodityDTO = commodityService.getCommodityById(commodityId);
            return ResponseEntity.ok(ApiResponse.success(commodityDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get commodity by commodity code
     * GET /api/v1/commodities/code/{commodityCode}
     */
    @GetMapping("/code/{commodityCode}")
    public ResponseEntity<ApiResponse<CommodityDTO>> getCommodityByCode(@PathVariable String commodityCode) {
        try {
            CommodityDTO commodityDTO = commodityService.getCommodityByCode(commodityCode);
            return ResponseEntity.ok(ApiResponse.success(commodityDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get all commodities with pagination
     * GET /api/v1/commodities?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommodityDTO>>> getAllCommodities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CommodityDTO> commodities = commodityService.getAllCommodities(pageable);
            return ResponseEntity.ok(ApiResponse.success(commodities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Update commodity
     * Admin only operation
     * PUT /api/v1/commodities/{commodityId}
     */
    @PutMapping("/{commodityId}")
    public ResponseEntity<ApiResponse<CommodityDTO>> updateCommodity(
            @PathVariable Long commodityId,
            @RequestBody AddCommodityRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            CommodityDTO commodityDTO = commodityService.updateCommodity(commodityId, request, userId);
            return ResponseEntity.ok(ApiResponse.success(commodityDTO, "Commodity updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Add quality grade to commodity
     * Admin only operation
     * POST /api/v1/commodities/{commodityId}/quality-grades
     */
    @PostMapping("/{commodityId}/quality-grades")
    public ResponseEntity<ApiResponse<QualityGradeDTO>> addQualityGrade(
            @PathVariable Long commodityId,
            @RequestBody AddQualityGradeRequest request) {
        try {
            QualityGradeDTO gradeDTO = commodityService.addQualityGrade(commodityId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(gradeDTO, "Quality grade added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    

    
    /**
     * Deactivate commodity
     * Admin only operation
     * PATCH /api/v1/commodities/{commodityId}/deactivate
     */
    @PatchMapping("/{commodityId}/deactivate")
    public ResponseEntity<ApiResponse<CommodityDTO>> deactivateCommodity(
            @PathVariable Long commodityId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            CommodityDTO commodityDTO = commodityService.deactivateCommodity(commodityId, userId);
            return ResponseEntity.ok(ApiResponse.success(commodityDTO, "Commodity deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Delete commodity from master
     * Admin only operation
     * DELETE /api/v1/commodities/{commodityId}
     */
    @DeleteMapping("/{commodityId}")
    public ResponseEntity<ApiResponse<Void>> deleteCommodity(
            @PathVariable Long commodityId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            commodityService.deleteCommodity(commodityId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Commodity deleted successfully from master"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
}
