package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.kalibyte.tradex.trading.dto.DealDTO;
import com.kalibyte.tradex.trading.dto.request.LockDealRequest;
import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.trading.service.InventoryLockEngine;
import com.kalibyte.tradex.common.dto.ApiResponse;

import java.util.List;

import com.kalibyte.tradex.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * DealController - Handles deal (matched agreements) operations
 * 
 * Endpoints:
 * - GET /api/v1/deals - Get my deals
 * - GET /api/v1/deals/{id} - Get deal details
 * - POST /api/v1/deals/{id}/lock - Lock deal (pessimistic locking)
 * 
 * Authorization:
 * - Get deals: CAN_BUY or CAN_SELL
 * - Lock deal: ROLE_ADMIN (only admins can lock inventory)
 */
@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class DealController {
    
    private final TradingService tradingService;
    private final InventoryLockEngine inventoryLockEngine;
    private final SecurityUtils securityUtils;
    
    /**
     * GET /api/v1/deals?role={BUYER|SELLER}
     * Get all deals for the current party
     * 
     * Query param 'role' determines whether to return buyer or seller deals
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getMyDeals(
            @RequestParam String role,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        
        Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
        List<DealDTO> deals = tradingService.getDealsForParty(partyId, role);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Deals retrieved successfully",
                deals
        ));
    }
    
    /**
     * GET /api/v1/deals/{id}
     * Get deal details
     * 
     * Returns complete deal information including buyer/seller,
     * quantities, rates, and lock status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealDTO>> getDeal(
            @PathVariable Long id) {
        
        DealDTO deal = tradingService.getDeal(id);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Deal retrieved successfully",
                deal
        ));
    }
    
    /**
     * POST /api/v1/deals/{id}/lock
     * Lock a deal (reserve inventory via pessimistic locking)
     * 
     * Request: LockDealRequest {
     *   notes: "Operator notes"
     * }
     * 
     * Authorization: ROLE_ADMIN only
     * 
     * This endpoint:
     * 1. Acquires pessimistic lock on supply row
     * 2. Decrements available, increments locked quantities
     * 3. Generates contract automatically
     * 4. Marks deal as LOCKED
     * 5. Returns updated deal with contract details
     * 
     * The operation is atomic and serializable.
     */
    @PostMapping({"/{id}/lock", "/{id}/approve"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DealDTO>> lockDeal(
            @PathVariable Long id,
            @RequestBody(required = false) LockDealRequest request,
            HttpServletRequest httpRequest) {
        
        String operatorName = securityUtils.resolveAdminName(httpRequest, "ADMIN-USER");
        DealDTO lockedDeal = inventoryLockEngine.lockDealAndIssueContract(id, operatorName);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Deal approved and locked successfully. Inventory reserved.",
                lockedDeal
        ));
    }
    
    /**
     * POST /api/v1/deals/{id}/reject
     * Reject a deal
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DealDTO>> rejectDeal(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Rejected by Admin") String reason,
            HttpServletRequest httpRequest) {
        
        String operatorName = securityUtils.resolveAdminName(httpRequest, "ADMIN-USER");
        DealDTO rejectedDeal = tradingService.rejectDeal(id, reason, operatorName);
        
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Deal rejected successfully.",
                rejectedDeal
        ));
    }
}
