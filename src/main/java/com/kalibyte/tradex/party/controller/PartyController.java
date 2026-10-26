package com.kalibyte.tradex.party.controller;

import com.kalibyte.tradex.party.dto.PartyDTO;
import com.kalibyte.tradex.party.dto.request.AddPartyRequest;
import com.kalibyte.tradex.common.dto.ApiResponse;
import com.kalibyte.tradex.party.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {
    
    private final PartyService partyService;
    
    /**
     * Add new party
     * Admin only operation
     * POST /api/v1/parties
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PartyDTO>> addParty(
            @RequestBody AddPartyRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.addParty(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(partyDTO, "Party created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get party by ID
     * GET /api/v1/parties/{partyId}
     */
    @GetMapping("/{partyId}")
    public ResponseEntity<ApiResponse<PartyDTO>> getPartyById(@PathVariable Long partyId) {
        try {
            PartyDTO partyDTO = partyService.getPartyById(partyId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get party by party code
     * GET /api/v1/parties/code/{partyCode}
     */
    @GetMapping("/code/{partyCode}")
    public ResponseEntity<ApiResponse<PartyDTO>> getPartyByCode(@PathVariable String partyCode) {
        try {
            PartyDTO partyDTO = partyService.getPartyByCode(partyCode);
            return ResponseEntity.ok(ApiResponse.success(partyDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Get all parties with pagination
     * GET /api/v1/parties?page=0&size=20&showDeleted=false
     * By default, returns only non-deleted parties. If showDeleted=true, returns deleted parties.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartyDTO>>> getAllParties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "false") Boolean showDeleted) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PartyDTO> parties = partyService.getAllParties(pageable, showDeleted);
            return ResponseEntity.ok(ApiResponse.success(parties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Update party
     * Admin only operation
     * PUT /api/v1/parties/{partyId}
     */
    @PutMapping("/{partyId}")
    public ResponseEntity<ApiResponse<PartyDTO>> updateParty(
            @PathVariable Long partyId,
            @RequestBody AddPartyRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.updateParty(partyId, request, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Deactivate party
     * Admin only operation
     * PATCH /api/v1/parties/{partyId}/deactivate
     */
    @PatchMapping("/{partyId}/deactivate")
    public ResponseEntity<ApiResponse<PartyDTO>> deactivateParty(
            @PathVariable Long partyId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.deactivateParty(partyId, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Soft delete party
     * Admin only operation
     * DELETE /api/v1/parties/{partyId}
     */
    @DeleteMapping("/{partyId}")
    public ResponseEntity<ApiResponse<PartyDTO>> deleteParty(
            @PathVariable Long partyId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.deleteParty(partyId, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party soft-deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Restore soft-deleted party
     * Admin only operation
     * PATCH /api/v1/parties/{partyId}/restore
     */
    @PatchMapping("/{partyId}/restore")
    public ResponseEntity<ApiResponse<PartyDTO>> restoreParty(
            @PathVariable Long partyId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.restoreParty(partyId, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party restored successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Activate party
     * Admin only operation
     * PATCH /api/v1/parties/{partyId}/activate
     */
    @PatchMapping("/{partyId}/activate")
    public ResponseEntity<ApiResponse<PartyDTO>> activateParty(
            @PathVariable Long partyId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.activateParty(partyId, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party activated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Approve party
     * Admin only operation
     * PATCH /api/v1/parties/{partyId}/approve
     */
    @PatchMapping("/{partyId}/approve")
    public ResponseEntity<ApiResponse<PartyDTO>> approveParty(
            @PathVariable Long partyId,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            PartyDTO partyDTO = partyService.activateParty(partyId, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party approved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    /**
     * Reject party
     * Admin only operation
     * PATCH /api/v1/parties/{partyId}/reject
     */
    @PatchMapping("/{partyId}/reject")
    public ResponseEntity<ApiResponse<PartyDTO>> rejectParty(
            @PathVariable Long partyId,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "ADMIN") String userId) {
        try {
            String rejectReason = reason;
            if ((rejectReason == null || rejectReason.trim().isEmpty()) && body != null) {
                rejectReason = body.get("reason");
            }
            PartyDTO partyDTO = partyService.rejectParty(partyId, rejectReason, userId);
            return ResponseEntity.ok(ApiResponse.success(partyDTO, "Party rejected successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
}

