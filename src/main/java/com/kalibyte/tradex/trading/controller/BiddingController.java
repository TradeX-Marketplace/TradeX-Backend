package com.kalibyte.tradex.trading.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.kalibyte.tradex.trading.dto.BidDTO;
import com.kalibyte.tradex.trading.dto.request.PlaceBidRequest;
import com.kalibyte.tradex.trading.dto.request.SubmitCounterOfferRequest;
import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.common.dto.ApiResponse;

import java.time.LocalDate;
import java.util.List;

import com.kalibyte.tradex.common.security.SecurityUtils;
import com.kalibyte.tradex.trading.service.DoubleBlindMasker;
import jakarta.servlet.http.HttpServletRequest;

/**
 * BiddingController - Handles bidding and counter-offer operations
 *
 * Endpoints:
 * - POST /api/v1/bids - Place a new bid
 * - POST /api/v1/bids/{id}/counter - Submit counter-offer
 *
 * Authorization:
 * - Place bid requires CAN_BUY authority
 * - Submit counter requires CAN_SELL authority
 */
@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BiddingController {

    private final TradingService tradingService;
    private final SecurityUtils securityUtils;
    private final DoubleBlindMasker masker;

    /**
     * POST /api/v1/bids
     * Place a new bid on a supply
     *
     * Request: PlaceBidRequest {
     *   supplyId, bidRate, quantity, destination, remarks
     * }
     *
     * Authorization: CAN_BUY
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CAN_BUY')")
    public ResponseEntity<ApiResponse<BidDTO>> placeBid(
            @Valid @RequestBody PlaceBidRequest request,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {

        Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
        BidDTO bid = tradingService.placeBid(request, partyId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Bid placed successfully",
                        bid
                ));
    }

    /**
     * POST /api/v1/bids/{id}/counter
     * Submit counter-offer on a bid
     *
     * Request: SubmitCounterOfferRequest {
     *   counterRate, remarks
     * }
     *
     * Authorization: CAN_SELL
     *
     * Triggered when seller wants to counter-propose a rate to buyer's bid
     */
    @PostMapping("/{id}/counter")
    @PreAuthorize("hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<BidDTO>> submitCounterOffer(
            @PathVariable Long id,
            @Valid @RequestBody SubmitCounterOfferRequest request) {

        BidDTO bid = tradingService.submitCounter(id, request);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Counter-offer submitted successfully",
                bid
        ));
    }

    /**
     * GET /api/v1/bids
     * Get all bids with pagination
     *
     * Query Parameters:
     *   - page: Page number (0-indexed, default 0)
     *   - size: Page size (default 20)
     *
     * Returns: Paginated list of all bids ordered by creation date (newest first)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BidDTO>>> getAllBids(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BidDTO> bids = tradingService.getAllBids(pageable);
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        bids.forEach(b -> masker.maskBid(b, isAdmin));

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Bids retrieved successfully",
                bids
        ));
    }

    /**
     * GET /api/v1/bids/{id}
     * Get bid by ID
     *
     * Path Parameters:
     *   - id: Bid ID
     *
     * Returns: BidDTO with all bid details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BidDTO>> getBidById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        BidDTO bid = tradingService.getBidById(id);
        boolean isAdmin = securityUtils.isAdmin(httpRequest);
        BidDTO masked = masker.maskBid(bid, isAdmin);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Bid retrieved successfully",
                masked
        ));
    }

    /**
     * GET /api/v1/bids/history/by-date
     * Get bid history for a specific date
     *
     * Query Parameters:
     *   - date: Date in format YYYY-MM-DD (e.g., 2024-01-15)
     *
     * Returns: List of all bids created on the specified date, ordered by creation time (newest first)
     */
    @GetMapping("/history/by-date")
    public ResponseEntity<ApiResponse<List<BidDTO>>> getBidsByDate(
            @RequestParam(name = "date") String dateStr,
            HttpServletRequest httpRequest) {

        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<BidDTO> bids = tradingService.getBidsByDate(date);
            boolean isAdmin = securityUtils.isAdmin(httpRequest);
            List<BidDTO> masked = masker.maskBids(bids, isAdmin);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Bid history for " + date + " retrieved successfully",
                    masked
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Invalid date format. Please use YYYY-MM-DD format",
                    null
            ));
        }
    }
}
