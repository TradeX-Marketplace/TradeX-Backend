package com.kalibyte.tradex.trading.controller;

import com.kalibyte.tradex.commodity.dto.CommodityDTO;
import com.kalibyte.tradex.commodity.service.CommodityService;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.common.security.SecurityUtils;
import com.kalibyte.tradex.party.entity.Party;
import com.kalibyte.tradex.trading.dto.BidDTO;
import com.kalibyte.tradex.trading.dto.ContractDTO;
import com.kalibyte.tradex.trading.dto.DealDTO;
import com.kalibyte.tradex.trading.dto.DemandDTO;
import com.kalibyte.tradex.trading.dto.NegotiationDTO;
import com.kalibyte.tradex.trading.dto.SupplyDTO;
import com.kalibyte.tradex.trading.entity.Contract;
import com.kalibyte.tradex.trading.repository.BidRepository;
import com.kalibyte.tradex.trading.repository.ContractRepository;
import com.kalibyte.tradex.trading.service.DoubleBlindMasker;
import com.kalibyte.tradex.trading.service.NegotiationEngine;
import com.kalibyte.tradex.trading.service.TradingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * AiMarketplaceController - Dedicated read-only AI agent integration controller
 * for Tradex commodity marketplace.
 *
 * Base Path: /api/ai
 *
 * Provides safe, read-only GET endpoints reusing existing services and repositories
 * while strictly maintaining double-blind privacy, authentication, and party-level
 * access restrictions.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiMarketplaceController {

    private final CommodityService commodityService;
    private final TradingService tradingService;
    private final NegotiationEngine negotiationEngine;
    private final ContractRepository contractRepository;
    private final BidRepository bidRepository;
    private final DoubleBlindMasker masker;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/ai/commodities
     * List available commodities with pagination.
     */
    @GetMapping("/commodities")
    public ResponseEntity<ApiResponse<Page<CommodityDTO>>> getCommodities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CommodityDTO> commodities = commodityService.getAllCommodities(pageable);
            return ResponseEntity.ok(ApiResponse.success(commodities, "Commodities retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving commodities: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve commodities: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/supplies
     * Retrieve available supplies with optional commodity filtering.
     * Applies double-blind masking for non-admin callers to protect seller identity.
     */
    @GetMapping("/supplies")
    public ResponseEntity<ApiResponse<List<SupplyDTO>>> getSupplies(
            @RequestParam(required = false) Long commodityId,
            HttpServletRequest httpRequest) {
        try {
            List<SupplyDTO> supplies;
            if (commodityId != null) {
                supplies = tradingService.getAvailableSupplies(commodityId);
            } else {
                supplies = tradingService.getAvailableSupplies();
            }

            boolean isAdmin = securityUtils.isAdmin(httpRequest);
            List<SupplyDTO> maskedSupplies = masker.maskSupplies(supplies, isAdmin);

            return ResponseEntity.ok(ApiResponse.success(maskedSupplies, "Supplies retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving supplies: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve supplies: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/demands
     * Retrieve active demands with optional commodity filtering.
     * Applies double-blind masking for non-admin callers to protect buyer identity.
     */
    @GetMapping("/demands")
    public ResponseEntity<ApiResponse<List<DemandDTO>>> getDemands(
            @RequestParam(required = false) Long commodityId,
            HttpServletRequest httpRequest) {
        try {
            List<DemandDTO> demands;
            if (commodityId != null) {
                demands = tradingService.getActiveDemands(commodityId);
            } else {
                demands = tradingService.getActiveDemands();
            }

            boolean isAdmin = securityUtils.isAdmin(httpRequest);
            List<DemandDTO> maskedDemands = masker.maskDemands(demands, isAdmin);

            return ResponseEntity.ok(ApiResponse.success(maskedDemands, "Demands retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving demands: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve demands: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/bids
     * Retrieve bids with pagination and double-blind masking.
     * If caller specifies their party ID or myBidsOnly=true, scopes exclusively to their own bids.
     * For general market bids, bidder identity is masked (e.g. BDR-XXXX).
     */
    @GetMapping("/bids")
    public ResponseEntity<ApiResponse<Page<BidDTO>>> getBids(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean myBidsOnly,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            boolean isAdmin = securityUtils.isAdmin(httpRequest);
            Page<BidDTO> bids;

            if (myBidsOnly && !isAdmin) {
                Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
                bids = bidRepository.findByPartyIdOrderByCreatedAtDesc(partyId, pageable)
                        .map(BidDTO::fromEntity);
            } else {
                bids = tradingService.getAllBids(pageable);
                if (!isAdmin) {
                    bids.forEach(b -> masker.maskBid(b, false));
                }
            }

            return ResponseEntity.ok(ApiResponse.success(bids, "Bids retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving bids: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve bids: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/negotiations
     * Retrieve active negotiations exclusively for the authenticated party.
     */
    @GetMapping("/negotiations")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<List<NegotiationDTO>>> getNegotiations(
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        try {
            Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
            List<NegotiationDTO> negotiations = negotiationEngine.getActiveNegotiations(partyId);
            return ResponseEntity.ok(ApiResponse.success(negotiations, "Negotiations retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving negotiations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve negotiations: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/deals?role=BUYER
     * Retrieve deals for the authenticated party by role (BUYER or SELLER).
     */
    @GetMapping("/deals")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getDeals(
            @RequestParam(defaultValue = "BUYER") String role,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        try {
            Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
            String normalizedRole = role != null ? role.trim().toUpperCase() : "BUYER";
            if (!"BUYER".equals(normalizedRole) && !"SELLER".equals(normalizedRole)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid role: must be BUYER or SELLER", "INVALID_REQUEST"));
            }

            List<DealDTO> deals = tradingService.getDealsForParty(partyId, normalizedRole);
            return ResponseEntity.ok(ApiResponse.success(deals, "Deals retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("Error retrieving deals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve deals: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * GET /api/ai/contracts/{id}
     * Retrieve contract details by ID with party-level access verification.
     * Non-admin callers can only view contracts where they are the buyer or seller.
     */
    @GetMapping("/contracts/{id}")
    @PreAuthorize("hasAuthority('CAN_BUY') OR hasAuthority('CAN_SELL')")
    public ResponseEntity<ApiResponse<ContractDTO>> getContractById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Party-Id", required = false) Long headerPartyId,
            HttpServletRequest httpRequest) {
        try {
            Optional<Contract> contractOpt = contractRepository.findById(id);
            if (contractOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Contract not found with id: " + id, "NOT_FOUND"));
            }

            Contract contract = contractOpt.get();
            boolean isAdmin = securityUtils.isAdmin(httpRequest);

            if (!isAdmin) {
                Long partyId = securityUtils.resolvePartyId(httpRequest, headerPartyId);
                boolean isAuthorized = false;

                if (contract.getDeal() != null) {
                    Party buyer = contract.getDeal().getBuyerParty();
                    Party seller = contract.getDeal().getSellerParty();
                    if ((buyer != null && partyId.equals(buyer.getId())) ||
                            (seller != null && partyId.equals(seller.getId()))) {
                        isAuthorized = true;
                    }
                }

                if (!isAuthorized) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied: You are not authorized to view this contract", "ACCESS_DENIED"));
                }
            }

            ContractDTO dto = ContractDTO.fromEntity(contract);
            return ResponseEntity.ok(ApiResponse.success(dto, "Contract retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving contract {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve contract: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
}
