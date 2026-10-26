package com.kalibyte.tradex.trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kalibyte.tradex.trading.dto.NegotiationDTO;
import com.kalibyte.tradex.trading.dto.request.SubmitNegotiationOfferRequest;
import com.kalibyte.tradex.trading.entity.*;
import com.kalibyte.tradex.trading.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NegotiationEngine - Multi-round price negotiation management
 * 
 * Manages the negotiation process between buyer and seller:
 * 1. Buyer makes initial offer (via Bid)
 * 2. Seller counters with their rate
 * 3. Buyer can accept counter or counter-counter
 * 4. Process repeats until agreement reached or negotiation expires
 * 
 * When buyer and seller rates converge (match), deal is automatically created.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NegotiationEngine {
    
    private final NegotiationRepository negotiationRepository;
    private final NegotiationRoundRepository negotiationRoundRepository;
    private final DealRepository dealRepository;
    private final BidRepository bidRepository;
    
    private static final BigDecimal CONVERGENCE_THRESHOLD = new BigDecimal("0.01"); // 0.01 rate units
    
    /**
     * Initiate a negotiation from a bid
     */
    public NegotiationDTO initiateNegotiation(Bid bid) {
        log.info("Initiating negotiation for bid: {}", bid.getId());
        
        Negotiation negotiation = Negotiation.builder()
                .bid(bid)
                .buyerParty(bid.getParty())
                .sellerParty(bid.getSupply().getParty())
                .currentOffer(bid.getBidRate())
                .buyerOffer(bid.getBidRate())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Negotiation saved = negotiationRepository.save(negotiation);
        
        // Create first round with buyer's initial bid
        NegotiationRound round1 = NegotiationRound.builder()
                .negotiation(saved)
                .roundNumber(1)
                .offeredBy("BUYER")
                .rate(bid.getBidRate())
                .remarks("Initial bid")
                .createdAt(LocalDateTime.now())
                .build();
        
        negotiationRoundRepository.save(round1);
        
        log.info("Negotiation created with ID: {}, First round bid rate: {}", 
                saved.getId(), bid.getBidRate());
        
        return NegotiationDTO.fromEntity(saved, List.of(round1));
    }
    
    /**
     * Submit an offer/counter-offer in a negotiation
     * 
     * @param negotiationId The negotiation thread
     * @param request The offer details (rate and remarks)
     * @param offeredBy Who offered (BUYER or SELLER)
     * @return Updated negotiation with all rounds
     */
    public NegotiationDTO submitOffer(Long negotiationId, 
                                       SubmitNegotiationOfferRequest request,
                                       String offeredBy) {
        log.info("Submitting offer for negotiation: {} by {}", negotiationId, offeredBy);
        
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found"));
        
        // Check if negotiation is still active
        if (!"ACTIVE".equals(negotiation.getStatus())) {
            log.warn("Negotiation {} is not active, status: {}", negotiationId, negotiation.getStatus());
            throw new IllegalStateException("Negotiation is no longer active");
        }
        
        // Get all previous rounds to determine next round number
        List<NegotiationRound> allRounds = negotiationRoundRepository
                .findByNegotiationIdOrderByRoundNumberAsc(negotiationId);
        
        int nextRoundNumber = allRounds.size() + 1;
        
        // Create new round
        NegotiationRound newRound = NegotiationRound.builder()
                .negotiation(negotiation)
                .roundNumber(nextRoundNumber)
                .offeredBy(offeredBy)
                .rate(request.getRate())
                .remarks(request.getRemarks())
                .createdAt(LocalDateTime.now())
                .build();
        
        negotiationRoundRepository.save(newRound);
        
        log.debug("Offer submitted. Round: {}, Rate: {}, By: {}", 
                nextRoundNumber, request.getRate(), offeredBy);
        
        // Update current offer
        negotiation.setCurrentOffer(request.getRate());
        negotiation.setUpdatedAt(LocalDateTime.now());
        
        // Check for convergence (agreement)
        boolean hasAgreed = checkAndHandleAgreement(negotiation, allRounds, newRound);
        
        if (hasAgreed) {
            log.info("Agreement reached in negotiation {}. Creating deal.", negotiationId);
            negotiation.setStatus("AGREED");
        }
        
        Negotiation updated = negotiationRepository.save(negotiation);
        
        // Fetch all rounds including the new one
        List<NegotiationRound> updatedRounds = negotiationRoundRepository
                .findByNegotiationIdOrderByRoundNumberAsc(negotiationId);
        
        return NegotiationDTO.fromEntity(updated, updatedRounds);
    }
    
    /**
     * Check if buyer and seller rates have converged (agreement reached)
     * 
     * Convergence occurs when:
     * - Seller's offered rate >= Buyer's maximum acceptable rate (or vice versa)
     * - The rates differ by less than CONVERGENCE_THRESHOLD
     */
    private boolean checkAndHandleAgreement(Negotiation negotiation, 
                                            List<NegotiationRound> previousRounds,
                                            NegotiationRound newRound) {
        if (previousRounds.isEmpty()) {
            return false; // Need at least one offer from other party
        }
        
        // Find last offer from the other party
        NegotiationRound lastCounterOffer = null;
        for (int i = previousRounds.size() - 1; i >= 0; i--) {
            if (!previousRounds.get(i).getOfferedBy().equals(newRound.getOfferedBy())) {
                lastCounterOffer = previousRounds.get(i);
                break;
            }
        }
        
        if (lastCounterOffer == null) {
            return false; // Still waiting for counterparty offer
        }
        
        // Check if rates converged (within threshold)
        BigDecimal diff = newRound.getRate().subtract(lastCounterOffer.getRate()).abs();
        
        log.debug("Rate comparison - New: {}, Last Counter: {}, Diff: {}", 
                newRound.getRate(), lastCounterOffer.getRate(), diff);
        
        return diff.compareTo(CONVERGENCE_THRESHOLD) <= 0;
    }
    
    /**
     * Get all active negotiations for a party
     */
    @Transactional(readOnly = true)
    public List<NegotiationDTO> getActiveNegotiations(Long partyId) {
        List<Negotiation> negotiations = negotiationRepository
                .findByBuyerPartyIdAndStatus(partyId, "ACTIVE");
        
        negotiations.addAll(negotiationRepository
                .findBySellerPartyIdAndStatus(partyId, "ACTIVE"));
        
        return negotiations.stream()
                .map(neg -> {
                    List<NegotiationRound> rounds = negotiationRoundRepository
                            .findByNegotiationIdOrderByRoundNumberAsc(neg.getId());
                    return NegotiationDTO.fromEntity(neg, rounds);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific negotiation with all its rounds
     */
    @Transactional(readOnly = true)
    public NegotiationDTO getNegotiation(Long negotiationId) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found"));
        
        List<NegotiationRound> rounds = negotiationRoundRepository
                .findByNegotiationIdOrderByRoundNumberAsc(negotiationId);
        
        return NegotiationDTO.fromEntity(negotiation, rounds);
    }
    
    /**
     * Reject a negotiation
     */
    public NegotiationDTO rejectNegotiation(Long negotiationId, String reason) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found"));
        
        negotiation.setStatus("REJECTED");
        negotiation.setUpdatedAt(LocalDateTime.now());
        
        Negotiation updated = negotiationRepository.save(negotiation);
        
        List<NegotiationRound> rounds = negotiationRoundRepository
                .findByNegotiationIdOrderByRoundNumberAsc(negotiationId);
        
        log.info("Negotiation {} rejected. Reason: {}", negotiationId, reason);
        
        return NegotiationDTO.fromEntity(updated, rounds);
    }
}
