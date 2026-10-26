package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.kalibyte.tradex.trading.dto.DemandDTO;
import com.kalibyte.tradex.trading.dto.request.PostDemandRequest;
import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.trading.service.DoubleBlindMasker;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * DemandController - Handles demand (buyer orders) operations
 * 
 * Endpoints:
 * - POST /api/v1/demands - Create new demand
 * - GET /api/v1/demands - Search active demands by commodity
 * - GET /api/v1/demands/{id} - Get demand details
 */
@RestController
@RequestMapping("/api/v1/demands")
@RequiredArgsConstructor
public class DemandController {
    
    private final TradingService tradingService;
    private final DoubleBlindMasker masker;
    private final SecurityUtils securityUtils;
    
    /**
     * POST /api/v1/demands
     * Create a new demand (buyer posts their procurement need)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DemandDTO>> createDemand(
            @Valid @RequestBody PostDemandRequest request,
            @RequestHeader(value = "X-Party-Id", required = false) Long partyId,
            HttpServletRequest httpRequest) {
        
        Long effectivePartyId = securityUtils.resolvePartyId(httpRequest, partyId);
        DemandDTO demand = tradingService.createDemand(request, effectivePartyId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Demand created successfully",
                        demand
                ));
    }
    
    /**
     * GET /api/v1/demands
     * Get active demands for a commodity or all active demands
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandDTO>>> getDemands(
            @RequestParam(required = false) Long commodityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        
        List<DemandDTO> demands;
        if (commodityId != null) {
            demands = tradingService.getActiveDemands(commodityId);
        } else {
            demands = tradingService.getActiveDemands();
        }
        
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        List<DemandDTO> maskedDemands = masker.maskDemands(demands, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.<List<DemandDTO>>builder()
                .success(true)
                .message("Demands retrieved successfully")
                .data(maskedDemands)
                .build());
    }
    
    /**
     * GET /api/v1/demands/{id}
     * Get demand details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandDTO>> getDemand(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        DemandDTO demand = tradingService.getDemand(id);
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        DemandDTO maskedDemand = masker.maskDemand(demand, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.<DemandDTO>builder()
                .success(true)
                .message("Demand retrieved successfully")
                .data(maskedDemand)
                .build());
    }
}
