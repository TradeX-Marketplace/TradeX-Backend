package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.kalibyte.tradex.trading.dto.NegotiationDTO;
import com.kalibyte.tradex.trading.dto.request.SubmitNegotiationOfferRequest;
import com.kalibyte.tradex.trading.service.NegotiationEngine;
import com.kalibyte.tradex.common.dto.ApiResponse;

import java.util.List;

import com.kalibyte.tradex.common.security.SecurityUtils;
import com.kalibyte.tradex.trading.stream.MarketStreamService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * NegotiationController - Handles multi-round price negotiation
 * 
 * Endpoints:
 * - GET /api/v1/negotiations - Get my active negotiations
 * - GET /api/v1/negotiations/{id} - Get negotiation details
 * - POST /api/v1/negotiations/{id}/offer - Submit offer/counter-offer
 * - POST /api/v1/negotiations/{id}/reject - Reject negotiation
 * 
 * Negotiation Flow:
 * 1. Buyer places bid (creates implicit negotiation thread)
 * 2. Seller counters with their rate
 * 3. Buyer can accept, counter-counter, or reject
 * 4. When rates converge within threshold, deal is auto-created
 */
@RestController
@RequestMapping("/api/v1/negotiations")
@RequiredArgsConstructor
public class NegotiationController {
    
    private final NegotiationEngine negotiationEngine;
    private final SecurityUtils securityUtils;
    private final MarketStreamService streamService;
    
    /**
     * GET /api/v1/negotiations
     * Get all active negotiations for the current party
     * 
     * Shows negotiations where user is either buyer or seller
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<List<NegotiationDTO>>> getMyNegotiations(
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        
        Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
        List<NegotiationDTO> negotiations = negotiationEngine.getActiveNegotiations(partyId);
        
        return ResponseEntity.ok(ApiResponse.<List<NegotiationDTO>>builder()
                .success(true)
                .message("Negotiations retrieved successfully")
                .data(negotiations)
                .build());
    }
    
    /**
     * GET /api/v1/negotiations/{id}
     * Get negotiation details with full history of offers
     * 
     * Returns all NegotiationRounds in chronological order
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NegotiationDTO>> getNegotiation(
            @PathVariable Long id) {
        
        NegotiationDTO negotiation = negotiationEngine.getNegotiation(id);
        
        return ResponseEntity.ok(ApiResponse.<NegotiationDTO>builder()
                .success(true)
                .message("Negotiation retrieved successfully")
                .data(negotiation)
                .build());
    }
    
    /**
     * POST /api/v1/negotiations/{id}/offer
     * Submit an offer or counter-offer in a negotiation
     * 
     * Request: SubmitNegotiationOfferRequest {
     *   rate, remarks
     * }
     * 
     * The endpoint automatically determines if this is an offer or counter
     * based on the party's role (buyer/seller) and existing rounds.
     * 
     * If offer converges with counterparty's last offer, deal is auto-created.
     */
    @PostMapping("/{id}/offer")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<NegotiationDTO>> submitOffer(
            @PathVariable Long id,
            @Valid @RequestBody SubmitNegotiationOfferRequest request,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            @RequestHeader(value = "X-Party-Role", required = false) String headerRole,
            HttpServletRequest httpRequest) {
        
        Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
        String offeredBy = securityUtils.resolvePartyRole(httpRequest, headerRole);
        
        NegotiationDTO negotiation = negotiationEngine.submitOffer(id, request, offeredBy);
        
        // Broadcast real-time update via SSE
        String eventName = "AGREED".equalsIgnoreCase(negotiation.getStatus()) ? "AGREEMENT_REACHED" : "OFFER_SUBMITTED";
        streamService.broadcastNegotiationUpdate(id, eventName, negotiation);
        
        return ResponseEntity.ok(ApiResponse.<NegotiationDTO>builder()
                .success(true)
                .message("Offer submitted successfully")
                .data(negotiation)
                .build());
    }
    
    /**
     * POST /api/v1/negotiations/{id}/reject
     * Reject a negotiation (end the discussion)
     * 
     * Request: { reason: "string" }
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<NegotiationDTO>> rejectNegotiation(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "No reason provided") String reason) {
        
        NegotiationDTO negotiation = negotiationEngine.rejectNegotiation(id, reason);
        streamService.broadcastNegotiationUpdate(id, "NEGOTIATION_REJECTED", negotiation);
        
        return ResponseEntity.ok(ApiResponse.<NegotiationDTO>builder()
                .success(true)
                .message("Negotiation rejected")
                .data(negotiation)
                .build());
    }
}
