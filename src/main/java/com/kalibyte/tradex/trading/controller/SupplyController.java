package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.kalibyte.tradex.trading.dto.SupplyDTO;
import com.kalibyte.tradex.trading.dto.request.PostSupplyRequest;
import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.trading.service.DoubleBlindMasker;
import com.kalibyte.tradex.common.dto.ApiResponse;

import com.kalibyte.tradex.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * SupplyController - Handles supply (seller commodity lot) operations
 * 
 * Endpoints:
 * - POST /api/v1/supplies - Create new supply
 * - GET /api/v1/supplies - Search active supplies by commodity
 * - GET /api/v1/supplies/{id} - Get supply details
 * 
 * Authorization:
 * - Create supply requires CAN_ADD_STOCK authority
 * - View applies double-blind masking for non-admin parties
 */
@RestController
@RequestMapping("/api/v1/supplies")
@RequiredArgsConstructor
public class SupplyController {
    
    private final TradingService tradingService;
    private final DoubleBlindMasker masker;
    private final SecurityUtils securityUtils;
    
    /**
     * POST /api/v1/supplies
     * Create a new supply (seller posts their commodity lot)
     * 
     * Request: PostSupplyRequest {
     *   commodityId, totalQuantity, askingRate, location, 
     *   originZone, labSpecs, validTill
     * }
     * 
     * Authorization: CAN_ADD_STOCK
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CAN_ADD_STOCK')")
    public ResponseEntity<ApiResponse<SupplyDTO>> createSupply(
            @Valid @RequestBody PostSupplyRequest request,
            @RequestHeader(value = "X-Party-Id", required = false) Long partyId,
            HttpServletRequest httpRequest) {
        Long effectivePartyId = securityUtils.resolvePartyId(httpRequest, partyId);
        SupplyDTO supply = tradingService.createSupply(request, effectivePartyId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Supply created successfully",
                        supply
                ));
    }
    
    /**
     * GET /api/v1/supplies?commodityId={id}&page={page}&size={size}
     * Get active supplies for a commodity (for buyer discovery)
     * If commodityId is omitted, returns all active supplies across commodities
     * 
     * Returns paginated list of supplies with double-blind masking applied
     * for non-admin parties
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplyDTO>>> getSupplies(
            @RequestParam(required = false) Long commodityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        
        List<SupplyDTO> supplies;
        if (commodityId != null) {
            supplies = tradingService.getAvailableSupplies(commodityId);
        } else {
            supplies = tradingService.getAvailableSupplies();
        }
        
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        List<SupplyDTO> maskedSupplies = masker.maskSupplies(supplies, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.<List<SupplyDTO>>builder()
                .success(true)
                .message("Supplies retrieved successfully")
                .data(maskedSupplies)
                .build());
    }
    
    /**
     * GET /api/v1/supplies/{id}
     * Get supply details
     * 
     * Returns supply with identity masking based on caller role:
     * - ADMIN: Full details including seller information
     * - PARTY: Masked seller identity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplyDTO>> getSupply(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        SupplyDTO supply = tradingService.getSupply(id);
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        SupplyDTO maskedSupply = masker.maskSupply(supply, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.<SupplyDTO>builder()
                .success(true)
                .message("Supply retrieved successfully")
                .data(maskedSupply)
                .build());
    }
}
