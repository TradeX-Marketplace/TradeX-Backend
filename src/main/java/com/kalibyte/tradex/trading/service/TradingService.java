package com.kalibyte.tradex.trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kalibyte.tradex.trading.dto.*;
import com.kalibyte.tradex.trading.dto.request.*;
import com.kalibyte.tradex.trading.entity.*;
import com.kalibyte.tradex.trading.repository.*;
import com.kalibyte.tradex.party.entity.Party;
import com.kalibyte.tradex.party.repository.PartyRepository;
import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.commodity.repository.CommodityRepository;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;
import com.kalibyte.tradex.notification.exception.WhatsAppException;
import com.kalibyte.tradex.notification.event.TradexNotificationEvents;
import com.kalibyte.tradex.trading.stream.MarketStreamService;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TradingService - Core business logic for commodity trading platform
 * 
 * Manages:
 * - Supply (Seller Lots)
 * - Demand (Buyer Orders)
 * - Bidding (Offer & Counter-Offer)
 * - Deal Matching (Buyer-Seller Agreements)
 * - Negotiation (Multi-Round Price Discussions)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TradingService {
    
    private final SupplyRepository supplyRepository;
    private final DemandRepository demandRepository;
    private final BidRepository bidRepository;
    private final NegotiationRepository negotiationRepository;
    private final NegotiationRoundRepository negotiationRoundRepository;
    private final DealRepository dealRepository;
    private final ContractRepository contractRepository;
    private final PartyRepository partyRepository;
    private final CommodityRepository commodityRepository;
    private final WhatsAppService whatsAppService;
    private final ApplicationEventPublisher eventPublisher;
    private final MarketStreamService marketStreamService;
    
    // ==================== SUPPLY MANAGEMENT ====================
    
    /**
     * Create a new supply (seller posts their commodity lot)
     */
    public SupplyDTO createSupply(PostSupplyRequest request, Long partyId) {
        Party seller = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
        
        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new IllegalArgumentException("Commodity not found"));
        
        // Resolve Origin Manufacturer:
        // 1. If explicit originManufacturerId was sent in request, resolve that party
        // 2. Else if explicit originManufacturerName was sent, use that
        // 3. Otherwise, if seller is a Manufacturer (or no manufacturer provided), default to seller
        Long mfgId = request.getOriginManufacturerId();
        String mfgName = request.getOriginManufacturerName();
        if (mfgId != null) {
            Party mfgParty = partyRepository.findById(mfgId).orElse(null);
            if (mfgParty != null) {
                mfgName = mfgParty.getCompanyName();
            }
        } else if (mfgName == null || mfgName.trim().isEmpty()) {
            boolean isSellerMfg = seller.getRoles() != null && seller.getRoles().stream()
                    .anyMatch(r -> "MANUFACTURER".equalsIgnoreCase(r.getName()));
            if (isSellerMfg) {
                mfgId = seller.getId();
                mfgName = seller.getCompanyName();
            }
        }

        Supply supply = Supply.builder()
                .party(seller)
                .commodity(commodity)
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .lockedQuantity(BigDecimal.ZERO)
                .askingRate(request.getAskingRate())
                .location(request.getLocation())
                .originZone(request.getOriginZone())
                .originManufacturerId(mfgId)
                .originManufacturerName(mfgName)
                .validTill(request.getValidTill())
                .labSpecs(request.getLabSpecs())
                .status("ACTIVE")
                .createdBy(seller.getPartyCode() != null ? seller.getPartyCode() : seller.getCompanyName())
                .createdAt(LocalDateTime.now())
                .build();
        
        Supply saved = supplyRepository.save(supply);
        
        // Send WhatsApp Market Alert Template asynchronously and broadcast on live market stream
        try {
            Set<String> sellerPhones = seller.getAllContactPhones();
            if (!sellerPhones.isEmpty()) {
                TradexTemplateDtos.NewListingTemplate template = TradexTemplateDtos.NewListingTemplate.builder()
                        .commodity(commodity.getCommodityName())
                        .quantity(saved.getTotalQuantity().toPlainString())
                        .unit(commodity.getDefaultUnit() != null ? commodity.getDefaultUnit() : "MT")
                        .rate(saved.getAskingRate().toPlainString())
                        .location(saved.getLocation() != null ? saved.getLocation() : "Market")
                        .availableFrom(saved.getValidTill() != null ? saved.getValidTill().toString() : LocalDate.now().toString())
                        .listingLink("https://kkcanvassing.com/party-login")
                        .build();
                for (String p : sellerPhones) {
                    eventPublisher.publishEvent(new TradexNotificationEvents.NewListingEvent(p, template));
                    log.info("Published async WhatsApp new listing event for seller {}", p);
                }
            }
            marketStreamService.broadcastMarketUpdate("NEW_LISTING", SupplyDTO.fromEntity(saved));
        } catch (Exception e) {
            log.error("Failed to publish async new listing event: {}", e.getMessage());
        }
        
        return SupplyDTO.fromEntity(saved);
    }
    
    /**
     * Get active supplies for a commodity (for buyer search)
     */
    @Transactional(readOnly = true)
    public List<SupplyDTO> getAvailableSupplies(Long commodityId) {
        return supplyRepository.findByCommodityIdAndStatusOrderByAskingRateAsc(commodityId, "ACTIVE")
                .stream()
                .map(SupplyDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific supply details
     */
    @Transactional(readOnly = true)
    public SupplyDTO getSupply(Long supplyId) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found"));
        return SupplyDTO.fromEntity(supply);
    }
    
    // ==================== DEMAND MANAGEMENT ====================
    
    /**
     * Create a new demand (buyer posts their procurement need)
     */
    public DemandDTO createDemand(PostDemandRequest request, Long partyId) {
        Party buyer = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
        
        Commodity commodity = commodityRepository.findById(request.getCommodityId())
                .orElseThrow(() -> new IllegalArgumentException("Commodity not found"));
        
        Demand demand = Demand.builder()
                .party(buyer)
                .commodity(commodity)
                .requiredQuantity(request.getQuantity())
                .fulfilledQuantity(BigDecimal.ZERO)
                .targetRate(request.getTargetRate())
                .destination(request.getDestination())
                .destinationZone(request.getDestinationZone())
                .validTill(request.getValidTill())
                .status("ACTIVE")
                .visibility("PUBLIC")
                .createdBy(buyer.getPartyCode() != null ? buyer.getPartyCode() : buyer.getCompanyName())
                .createdAt(LocalDateTime.now())
                .build();
        
        Demand saved = demandRepository.save(demand);

        // Publish async WhatsApp notification: Buyer confirmation & seller opportunity alert
        try {
            String demandNum = saved.getDemandNumber() != null ? saved.getDemandNumber() : "DEM-" + saved.getId();
            String unit = commodity.getDefaultUnit() != null ? commodity.getDefaultUnit() : "MT";

            TradexTemplateDtos.DemandCreatedTemplate buyerTemplate = null;
            if (buyer.getPhone() != null && !buyer.getPhone().trim().isEmpty()) {
                buyerTemplate = TradexTemplateDtos.DemandCreatedTemplate.builder()
                        .demandNumber(demandNum)
                        .commodity(commodity.getCommodityName())
                        .quantity(saved.getRequiredQuantity().toPlainString())
                        .unit(unit)
                        .targetRate(saved.getTargetRate().toPlainString())
                        .destination(saved.getDestination() != null ? saved.getDestination() : "Unspecified")
                        .validTill(saved.getValidTill() != null ? saved.getValidTill().toString() : "Open")
                        .demandLink("https://kkcanvassing.com/party-login")
                        .build();
            }

            TradexTemplateDtos.DemandAlertTemplate alertTemplate = TradexTemplateDtos.DemandAlertTemplate.builder()
                    .commodity(commodity.getCommodityName())
                    .requiredQuantity(saved.getRequiredQuantity().toPlainString())
                    .unit(unit)
                    .targetRate(saved.getTargetRate().toPlainString())
                    .destinationZone(saved.getDestinationZone() != null ? saved.getDestinationZone() : "All")
                    .demandId(demandNum)
                    .quoteLink("https://kkcanvassing.com/party-login")
                    .build();

            eventPublisher.publishEvent(TradexNotificationEvents.DemandCreatedEvent.builder()
                    .buyerPhone(buyer.getPhone())
                    .buyerTemplate(buyerTemplate)
                    .sellerPhones(null)
                    .alertTemplate(alertTemplate)
                    .build());
            log.info("Published async WhatsApp demand created event for demand {}", demandNum);
        } catch (Exception e) {
            log.error("Failed to publish async WhatsApp notification for demand: {}", saved.getDemandNumber(), e);
        }

        return DemandDTO.fromEntity(saved);
    }
    
    /**
     * Get active demands for a commodity (for seller search)
     */
    @Transactional(readOnly = true)
    public List<DemandDTO> getActiveDemands(Long commodityId) {
        return demandRepository.findByCommodityIdAndStatusOrderByCreatedAtDesc(commodityId, "ACTIVE")
                .stream()
                .map(DemandDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific demand details
     */
    @Transactional(readOnly = true)
    public DemandDTO getDemand(Long demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found"));
        return DemandDTO.fromEntity(demand);
    }
    
    // ==================== BIDDING ====================
    
    /**
     * Place a new bid on a supply or demand
     */
    public BidDTO placeBid(PlaceBidRequest request, Long partyId) {
        Party bidder = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
        
        Supply supply = supplyRepository.findById(request.getSupplyId())
                .orElseThrow(() -> new IllegalArgumentException("Supply not found"));
        
        Bid bid = Bid.builder()
                .commodity(supply.getCommodity())
                .party(bidder)
                .supply(supply)
                .bidRate(request.getBidRate())
                .quantity(request.getQuantity())
                .destination(request.getDestination())
                .remarks(request.getRemarks())
                .status("PENDING")
                .bidType("BUY")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Bid saved = bidRepository.save(bid);
        
        // Publish async WhatsApp event: Bid Confirmation to Buyer & Bid Alert to Seller
        try {
            String bidIdentifier = saved.getBidNumber() != null ? saved.getBidNumber() : "BID-" + saved.getId();
            String unit = supply.getCommodity().getDefaultUnit() != null ? supply.getCommodity().getDefaultUnit() : "MT";
            
            TradexTemplateDtos.BidSubmittedTemplate buyerTemplate = null;
            if (bidder.getPhone() != null && !bidder.getPhone().trim().isEmpty()) {
                buyerTemplate = TradexTemplateDtos.BidSubmittedTemplate.builder()
                        .commodity(supply.getCommodity().getCommodityName())
                        .bidQuantity(saved.getQuantity().toPlainString())
                        .unit(unit)
                        .bidRate(saved.getBidRate().toPlainString())
                        .bidId(bidIdentifier)
                        .bidLink("https://kkcanvassing.com/party-login")
                        .build();
            }
            
            Party seller = supply.getParty();
            TradexTemplateDtos.BidReceivedTemplate sellerTemplate = null;
            if (seller != null && seller.getPhone() != null && !seller.getPhone().trim().isEmpty()) {
                sellerTemplate = TradexTemplateDtos.BidReceivedTemplate.builder()
                        .commodity(supply.getCommodity().getCommodityName())
                        .bidQuantity(saved.getQuantity().toPlainString())
                        .unit(unit)
                        .bidRate(saved.getBidRate().toPlainString())
                        .bidId(bidIdentifier)
                        .reviewLink("https://kkcanvassing.com/party-login")
                        .build();
            }

            eventPublisher.publishEvent(TradexNotificationEvents.BidPlacedEvent.builder()
                    .buyerPhone(bidder.getPhone())
                    .buyerTemplate(buyerTemplate)
                    .sellerPhone(seller != null ? seller.getPhone() : null)
                    .sellerTemplate(sellerTemplate)
                    .build());
            log.info("Published async WhatsApp bid placed event for bid {}", bidIdentifier);
        } catch (Exception e) {
            log.error("Failed to publish async WhatsApp notification for bid: {}", saved.getBidNumber(), e);
        }
        
        return BidDTO.fromEntity(saved);
    }
    
    /**
     * Submit counter-offer on a bid
     */
    public BidDTO submitCounter(Long bidId, SubmitCounterOfferRequest request) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found"));
        
        bid.setCounterRate(request.getCounterRate());
        bid.setCounterRemarks(request.getRemarks());
        bid.setStatus("COUNTERED");
        bid.setUpdatedAt(LocalDateTime.now());
        
        Bid updated = bidRepository.save(bid);
        
        // Publish async WhatsApp event for counter offer
        try {
            Party bidder = bid.getParty();
            Set<String> bidderPhones = bidder != null ? bidder.getAllContactPhones() : java.util.Collections.emptySet();
            if (!bidderPhones.isEmpty()) {
                TradexTemplateDtos.CounterOfferTemplate template = TradexTemplateDtos.CounterOfferTemplate.builder()
                        .commodity(bid.getCommodity().getCommodityName())
                        .quantity(bid.getQuantity().toPlainString())
                        .unit(bid.getCommodity().getDefaultUnit() != null ? bid.getCommodity().getDefaultUnit() : "MT")
                        .counterRate(updated.getCounterRate().toPlainString())
                        .negotiationLink("https://kkcanvassing.com/party-login")
                        .build();
                for (String bPhone : bidderPhones) {
                    eventPublisher.publishEvent(new TradexNotificationEvents.CounterOfferEvent(bPhone, template));
                    log.info("Published async WhatsApp counter offer event for bidder {}", bPhone);
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish async WhatsApp counter offer notification: {}", e.getMessage());
        }
        
        return BidDTO.fromEntity(updated);
    }
    
    // ==================== DEAL MATCHING ====================
    
    /**
     * Match a bid with a demand to create a deal
     * This creates an initial deal in MATCHED status (not yet locked)
     */
    public DealDTO matchDeal(Long bidId, Long demandId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found"));
        
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Demand not found"));
        
        Supply supply = bid.getSupply();
        Party buyer = bid.getParty();
        Party seller = supply.getParty();
        
        // Use final negotiated rate (counter rate if exists, else bid rate)
        BigDecimal finalRate = bid.getCounterRate() != null ? bid.getCounterRate() : bid.getBidRate();
        
        Deal deal = Deal.builder()
                .supply(supply)
                .demand(demand)
                .bid(bid)
                .buyerParty(buyer)
                .sellerParty(seller)
                .quantity(bid.getQuantity())
                .rate(finalRate)
                .lockStatus("UNLOCKED")
                .status("MATCHED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Deal saved = dealRepository.save(deal);
        
        // Update bid status
        bid.setStatus("MATCHED");
        bidRepository.save(bid);
        
        // Publish async WhatsApp events: Deal Agreed to Buyer & Deal Confirmed to Seller
        try {
            String dealIdentifier = saved.getDealNumber() != null ? saved.getDealNumber() : "DEAL-" + saved.getId();
            String unit = supply.getCommodity().getDefaultUnit() != null ? supply.getCommodity().getDefaultUnit() : "MT";
            
            TradexTemplateDtos.DealAgreedTemplate buyerTemplate = null;
            if (buyer != null && buyer.getPhone() != null && !buyer.getPhone().trim().isEmpty()) {
                buyerTemplate = TradexTemplateDtos.DealAgreedTemplate.builder()
                        .commodity(supply.getCommodity().getCommodityName())
                        .quantity(saved.getQuantity().toPlainString())
                        .unit(unit)
                        .finalRate(saved.getRate().toPlainString())
                        .dealId(dealIdentifier)
                        .dealLink("https://kkcanvassing.com/party-login")
                        .build();
            }
            
            TradexTemplateDtos.DealConfirmedTemplate sellerTemplate = null;
            if (seller != null && seller.getPhone() != null && !seller.getPhone().trim().isEmpty()) {
                sellerTemplate = TradexTemplateDtos.DealConfirmedTemplate.builder()
                        .commodity(supply.getCommodity().getCommodityName())
                        .quantity(saved.getQuantity().toPlainString())
                        .unit(unit)
                        .finalRate(saved.getRate().toPlainString())
                        .dealId(dealIdentifier)
                        .dealLink("https://kkcanvassing.com/party-login")
                        .build();
            }

            eventPublisher.publishEvent(TradexNotificationEvents.DealMatchedEvent.builder()
                    .buyerPhone(buyer != null ? buyer.getPhone() : null)
                    .buyerTemplate(buyerTemplate)
                    .sellerPhone(seller != null ? seller.getPhone() : null)
                    .sellerTemplate(sellerTemplate)
                    .build());
            log.info("Published async WhatsApp deal matched event for deal {}", dealIdentifier);
        } catch (Exception e) {
            log.error("Failed to publish async WhatsApp notification for deal match: {}", saved.getDealNumber(), e);
        }
        
        return DealDTO.fromEntity(saved);
    }
    
    /**
     * Get all deals involving a party
     */
    @Transactional(readOnly = true)
    public List<DealDTO> getDealsForParty(Long partyId, String role) {
        List<Deal> deals;
        if ("BUYER".equals(role)) {
            deals = dealRepository.findByBuyerPartyIdOrderByCreatedAtDesc(partyId);
        } else if ("SELLER".equals(role)) {
            deals = dealRepository.findBySellerPartyIdOrderByCreatedAtDesc(partyId);
        } else {
            throw new IllegalArgumentException("Invalid role");
        }
        return deals.stream()
                .map(DealDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific deal
     */
    @Transactional(readOnly = true)
    public DealDTO getDeal(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        return DealDTO.fromEntity(deal);
    }
    
    /**
     * Reject a deal
     */
    public DealDTO rejectDeal(Long dealId, String reason, String operatorName) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        deal.setStatus("REJECTED");
        deal.setUpdatedAt(LocalDateTime.now());
        Deal saved = dealRepository.save(deal);
        log.info("Deal {} rejected by {} with reason: {}", dealId, operatorName, reason);
        return DealDTO.fromEntity(saved);
    }
    
    // ==================== BID RETRIEVAL ====================
    
    /**
     * Get all bids (paginated)
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<BidDTO> getAllBids(org.springframework.data.domain.Pageable pageable) {
        return bidRepository.findAllBidsOrderByCreatedAtDesc(pageable)
                .map(BidDTO::fromEntity);
    }
    
    /**
     * Get bid by ID
     */
    @Transactional(readOnly = true)
    public BidDTO getBidById(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));
        return BidDTO.fromEntity(bid);
    }
    
    /**
     * Get all bids for a specific date (bid history by date)
     */
    @Transactional(readOnly = true)
    public List<BidDTO> getBidsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        return bidRepository.findBidsByDate(startOfDay)
                .stream()
                .map(BidDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    // ==================== WHATSAPP NOTIFICATIONS ====================
    
    /**
     * Send WhatsApp notification to seller when a new bid is placed
     */
    private void notifySellerOfNewBid(Party seller, Bid bid, Supply supply) throws WhatsAppException {
        if (seller == null) return;
        Set<String> phones = seller.getAllContactPhones();
        if (phones.isEmpty()) {
            log.warn("Cannot send bid notification - seller has no phone number. SellerId: {}", seller.getId());
            return;
        }
        
        for (String phone : phones) {
            whatsAppService.notifyBidEvent(
                formatPhoneNumber(phone),
                bid.getBidNumber(),
                supply.getCommodity().getCommodityName(),
                "PLACED",
                "New bid received: " + bid.getBidRate(),
                bid.getId()
            );
            log.info("Sent bid notification to seller. Phone: {}, BidNumber: {}", phone, bid.getBidNumber());
        }
    }
    
    /**
     * Send WhatsApp notification to buyer when deal is matched
     */
    private void notifyBuyerOfDealMatch(Party buyer, Deal deal, Supply supply) throws WhatsAppException {
        if (buyer == null) return;
        Set<String> phones = buyer.getAllContactPhones();
        if (phones.isEmpty()) {
            log.warn("Cannot send deal notification - buyer has no phone number. BuyerId: {}", buyer.getId());
            return;
        }
        
        for (String phone : phones) {
            whatsAppService.notifyDealEvent(
                formatPhoneNumber(phone),
                deal.getDealNumber(),
                supply.getCommodity().getCommodityName(),
                "MATCHED",
                String.format("Quantity: %s at rate: %s", deal.getQuantity(), deal.getRate()),
                deal.getId()
            );
            log.info("Sent deal match notification to buyer. Phone: {}, DealNumber: {}", phone, deal.getDealNumber());
        }
    }
    
    /**
     * Send WhatsApp notification to seller when deal is matched
     */
    private void notifySellerOfDealMatch(Party seller, Deal deal, Supply supply) throws WhatsAppException {
        if (seller == null) return;
        Set<String> phones = seller.getAllContactPhones();
        if (phones.isEmpty()) {
            log.warn("Cannot send deal notification - seller has no phone number. SellerId: {}", seller.getId());
            return;
        }
        
        for (String phone : phones) {
            whatsAppService.notifyDealEvent(
                formatPhoneNumber(phone),
                deal.getDealNumber(),
                supply.getCommodity().getCommodityName(),
                "MATCHED",
                String.format("Quantity: %s at rate: %s", deal.getQuantity(), deal.getRate()),
                deal.getId()
            );
            log.info("Sent deal match notification to seller. Phone: {}, DealNumber: {}", phone, deal.getDealNumber());
        }
    }
    
    /**
     * Format phone number to international format (+91)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        
        // Remove all non-digits
        phone = phone.replaceAll("\\D", "");
        
        // If already has country code (12 digits for +91)
        if (phone.length() == 12 && phone.startsWith("91")) {
            return "+" + phone;
        }
        
        // If only 10 digits (Indian mobile)
        if (phone.length() == 10) {
            return "+91" + phone;
        }
        
        // Already formatted or has country code
        if (phone.length() == 11 && !phone.startsWith("0")) {
            return "+" + phone;
        }
        
        // Return as-is if 11 digits starting with 0 (remove 0)
        if (phone.length() == 11 && phone.startsWith("0")) {
            return "+" + phone.substring(1);
        }
        
        return "+" + phone;
    }
    
    // ==================== CROSS-COMMODITY SEARCH ====================
    
    /**
     * Get all active supplies across all commodities (when commodityId is null)
     */
    @Transactional(readOnly = true)
    public List<SupplyDTO> getAvailableSupplies() {
        // Use findAll with filter in code since repository doesn't have a cross-commodity method
        return supplyRepository.findAll()
                .stream()
                .filter(supply -> "ACTIVE".equals(supply.getStatus()))
                .sorted((s1, s2) -> s1.getAskingRate().compareTo(s2.getAskingRate()))
                .map(SupplyDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all active demands across all commodities (when commodityId is null)
     */
    @Transactional(readOnly = true)
    public List<DemandDTO> getActiveDemands() {
        // Use findAll with filter in code since repository doesn't have a cross-commodity method
        return demandRepository.findAll()
                .stream()
                .filter(demand -> "ACTIVE".equals(demand.getStatus()))
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .map(DemandDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
