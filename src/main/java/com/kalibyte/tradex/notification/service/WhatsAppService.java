package com.kalibyte.tradex.notification.service;

import com.kalibyte.tradex.notification.client.WhatsAppApiClient;
import com.kalibyte.tradex.notification.config.WhatsAppConfig;
import com.kalibyte.tradex.notification.dto.WhatsAppMessageRequest;
import com.kalibyte.tradex.notification.dto.WhatsAppMessageResponse;
import com.kalibyte.tradex.notification.exception.WhatsAppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WhatsApp Service
 * 
 * High-level API for sending WhatsApp messages using BookBey API
 * Handles message composition, validation, and delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WhatsAppService {
    
    private final WhatsAppApiClient whatsAppApiClient;
    private final WhatsAppConfig whatsAppConfig;
    
    /**
     * Send WhatsApp message
     * 
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @return WhatsAppMessageResponse
     * @throws WhatsAppException if sending fails
     */
    public WhatsAppMessageResponse sendMessage(String phoneNumber, String message) 
            throws WhatsAppException {
        
        return sendMessage(phoneNumber, message, null, null);
    }
    
    /**
     * Send WhatsApp message with reference tracking
     * 
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @param referenceId Reference ID for tracking (bid ID, deal ID, etc.)
     * @param referenceType Reference type (BID, DEAL, SUPPLY, DEMAND, etc.)
     * @return WhatsAppMessageResponse
     * @throws WhatsAppException if sending fails
     */
    public WhatsAppMessageResponse sendMessage(String phoneNumber, String message,
                                               String referenceId, String referenceType) 
            throws WhatsAppException {
        
        if (!whatsAppConfig.isEnabled()) {
            log.debug("WhatsApp messaging is disabled in configuration");
            return createDisabledResponse();
        }
        
        // Validate inputs
        validateMessage(phoneNumber, message);
        
        // Create request
        WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                .phone(phoneNumber)
                .message(message)
                .messageType("TEXT")
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        
        // Send via API client
        try {
            WhatsAppMessageResponse response = whatsAppApiClient.sendMessage(request);
            log.info("WhatsApp message sent successfully. " +
                    "Phone: {}, ReferenceId: {}, ReferenceType: {}, MessageId: {}",
                    phoneNumber, referenceId, referenceType, response.getMessageId());
            return response;
        } catch (WhatsAppException e) {
            log.error("Failed to send WhatsApp message to: {}. Error: {}", 
                     phoneNumber, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Send WhatsApp message with media
     * 
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @param mediaUrl URL of media (image, document, etc.)
     * @param mediaType Type of media (IMAGE, DOCUMENT, AUDIO, VIDEO)
     * @param referenceId Reference ID for tracking
     * @param referenceType Reference type
     * @return WhatsAppMessageResponse
     * @throws WhatsAppException if sending fails
     */
    public WhatsAppMessageResponse sendMediaMessage(String phoneNumber, String message,
                                                     String mediaUrl, String mediaType,
                                                     String referenceId, String referenceType) 
            throws WhatsAppException {
        
        if (!whatsAppConfig.isEnabled()) {
            log.debug("WhatsApp messaging is disabled in configuration");
            return createDisabledResponse();
        }
        
        // Validate inputs
        validateMessage(phoneNumber, message);
        validateMediaUrl(mediaUrl, mediaType);
        
        // Create request
        WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                .phone(phoneNumber)
                .message(message)
                .messageType(mediaType != null ? mediaType.toUpperCase() : "IMAGE")
                .mediaUrl(mediaUrl)
                .caption(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        
        // Send via API client
        try {
            WhatsAppMessageResponse response = whatsAppApiClient.sendMessage(request);
            log.info("WhatsApp media message sent successfully. " +
                    "Phone: {}, MediaType: {}, ReferenceId: {}",
                    phoneNumber, mediaType, referenceId);
            return response;
        } catch (WhatsAppException e) {
            log.error("Failed to send WhatsApp media message to: {}. Error: {}", 
                     phoneNumber, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send generic approved WhatsApp Cloud API Template
     * 
     * @param phoneNumber Recipient phone number
     * @param templateName Approved BookBey template name
     * @param lang Language code (e.g., en_US)
     * @param params Ordered template variables
     * @param referenceId Optional reference ID
     * @param referenceType Optional reference type
     * @return WhatsAppMessageResponse
     */
    public WhatsAppMessageResponse sendTemplateMessage(String phoneNumber, String templateName,
                                                       String lang, java.util.List<String> params,
                                                       String referenceId, String referenceType) 
            throws WhatsAppException {
        return sendTemplateMessage(phoneNumber, templateName, lang, params, referenceId, referenceType, null);
    }

    public WhatsAppMessageResponse sendTemplateMessage(String phoneNumber, String templateName,
                                                       String lang, java.util.List<String> params,
                                                       String referenceId, String referenceType,
                                                       String formattedText) 
            throws WhatsAppException {
        
        if (!whatsAppConfig.isEnabled()) {
            log.debug("WhatsApp messaging is disabled in configuration");
            return createDisabledResponse();
        }
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new WhatsAppException("Phone number is required", "INVALID_PHONE");
        }
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new WhatsAppException("Template name is required", "INVALID_TEMPLATE");
        }
        
        WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                .phone(phoneNumber)
                .messageType("template")
                .template(templateName)
                .lang(lang != null ? lang : "en_US")
                .params(params != null ? params : java.util.Collections.emptyList())
                .message(formattedText)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        
        try {
            WhatsAppMessageResponse response = whatsAppApiClient.sendMessage(request);
            log.info("WhatsApp template [{}] sent successfully to {}. MessageId: {}", 
                     templateName, phoneNumber, response.getMessageId());
            return response;
        } catch (WhatsAppException e) {
            log.error("Failed to send WhatsApp template [{}] to: {}. Error: {}", 
                      templateName, phoneNumber, e.getMessage(), e);
            throw e;
        }
    }

    // ==================== 12 TYPED TEMPLATE METHODS ====================

    /** 1. Party Activation: tradex_party_activation */
    public WhatsAppMessageResponse sendPartyActivation(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PartyActivationTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.PARTY_ACTIVATION.getTemplateName(),
                "en_US", template.toParams(), null, "PARTY_ACTIVATION", template.toFormattedText());
    }

    /** 2. New Market Listing: tradex_new_listing */
    public WhatsAppMessageResponse sendNewListing(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NewListingTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.NEW_LISTING.getTemplateName(),
                "en_US", template.toParams(), null, "MARKET_LISTING", template.toFormattedText());
    }

    /** 3. Commodity Rate Updated: tradex_rate_updated */
    public WhatsAppMessageResponse sendRateUpdated(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.RateUpdatedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.RATE_UPDATED.getTemplateName(),
                "en_US", template.toParams(), null, "RATE_UPDATE", template.toFormattedText());
    }

    /** 4. Bid Submitted: tradex_bid_submitted */
    public WhatsAppMessageResponse sendBidSubmitted(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidSubmittedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.BID_SUBMITTED.getTemplateName(),
                "en_US", template.toParams(), template.getBidId(), "BID", template.toFormattedText());
    }

    /** 5. Bid Received: tradex_bid_received */
    public WhatsAppMessageResponse sendBidReceived(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidReceivedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.BID_RECEIVED.getTemplateName(),
                "en_US", template.toParams(), template.getBidId(), "BID", template.toFormattedText());
    }

    /** 6. Counter Offer: tradex_counter_offer */
    public WhatsAppMessageResponse sendCounterOffer(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.CounterOfferTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.COUNTER_OFFER.getTemplateName(),
                "en_US", template.toParams(), null, "NEGOTIATION", template.toFormattedText());
    }

    /** 7. Deal Agreed: tradex_deal_agreed */
    public WhatsAppMessageResponse sendDealAgreed(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealAgreedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DEAL_AGREED.getTemplateName(),
                "en_US", template.toParams(), template.getDealId(), "DEAL", template.toFormattedText());
    }

    /** 8. Deal Confirmed: tradex_deal_confirmed */
    public WhatsAppMessageResponse sendDealConfirmed(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealConfirmedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DEAL_CONFIRMED.getTemplateName(),
                "en_US", template.toParams(), template.getDealId(), "DEAL", template.toFormattedText());
    }

    /** 9. Quantity Locked: tradex_quantity_locked */
    public WhatsAppMessageResponse sendQuantityLocked(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.QuantityLockedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.QUANTITY_LOCKED.getTemplateName(),
                "en_US", template.toParams(), template.getDealId(), "LOCK", template.toFormattedText());
    }

    /** 10. Contract Ready: tradex_contract_ready */
    public WhatsAppMessageResponse sendContractReady(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractReadyTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.CONTRACT_READY.getTemplateName(),
                "en_US", template.toParams(), template.getContractNumber(), "CONTRACT", template.toFormattedText());
    }

    /** 11. Contract Approved: tradex_contract_approved */
    public WhatsAppMessageResponse sendContractApproved(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractApprovedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.CONTRACT_APPROVED.getTemplateName(),
                "en_US", template.toParams(), template.getContractNumber(), "CONTRACT", template.toFormattedText());
    }

    /** 12. Contract Locked: tradex_contract_locked */
    public WhatsAppMessageResponse sendContractLocked(String phoneNumber, 
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractLockedTemplate template) {
        return sendTemplateMessage(phoneNumber, 
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.CONTRACT_LOCKED.getTemplateName(),
                "en_US", template.toParams(), template.getContractNumber(), "CONTRACT", template.toFormattedText());
    }

    /** 13. Demand Created: tradex_demand_created */
    public WhatsAppMessageResponse sendDemandCreated(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandCreatedTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DEMAND_CREATED.getTemplateName(),
                "en_US", template.toParams(), template.getDemandNumber(), "DEMAND", template.toFormattedText());
    }

    /** 14. Demand Alert: tradex_demand_alert */
    public WhatsAppMessageResponse sendDemandAlert(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandAlertTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DEMAND_ALERT.getTemplateName(),
                "en_US", template.toParams(), template.getDemandId(), "DEMAND_ALERT", template.toFormattedText());
    }

    /** 15. Bid Accepted: tradex_bid_accepted */
    public WhatsAppMessageResponse sendBidAccepted(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidAcceptedTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.BID_ACCEPTED.getTemplateName(),
                "en_US", template.toParams(), template.getBidNumber(), "BID", template.toFormattedText());
    }

    /** 16. Bid Rejected: tradex_bid_rejected */
    public WhatsAppMessageResponse sendBidRejected(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidRejectedTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.BID_REJECTED.getTemplateName(),
                "en_US", template.toParams(), template.getBidNumber(), "BID", template.toFormattedText());
    }

    /** 17. Negotiation Rejected: tradex_negotiation_rejected */
    public WhatsAppMessageResponse sendNegotiationRejected(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NegotiationRejectedTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.NEGOTIATION_REJECTED.getTemplateName(),
                "en_US", template.toParams(), template.getNegotiationCode(), "NEGOTIATION", template.toFormattedText());
    }

    /** 18. Deal Cancelled: tradex_deal_cancelled */
    public WhatsAppMessageResponse sendDealCancelled(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealCancelledTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DEAL_CANCELLED.getTemplateName(),
                "en_US", template.toParams(), template.getDealNumber(), "DEAL", template.toFormattedText());
    }

    /** 19. Dispatch Update: tradex_dispatch_update */
    public WhatsAppMessageResponse sendDispatchUpdate(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DispatchUpdateTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.DISPATCH_UPDATE.getTemplateName(),
                "en_US", template.toParams(), template.getContractNumber(), "DISPATCH", template.toFormattedText());
    }

    /** 20. Payment Update: tradex_payment_update */
    public WhatsAppMessageResponse sendPaymentUpdate(String phoneNumber,
            com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PaymentUpdateTemplate template) {
        return sendTemplateMessage(phoneNumber,
                com.kalibyte.tradex.notification.dto.template.WhatsAppTemplateType.PAYMENT_UPDATE.getTemplateName(),
                "en_US", template.toParams(), template.getContractNumber(), "PAYMENT", template.toFormattedText());
    }
    
    /**
     * Send notification for bid event
     */
    public WhatsAppMessageResponse notifyBidEvent(String phoneNumber, String bidNumber,
                                                   String commodityName, String eventType,
                                                   String additionalInfo, Long bidId) 
            throws WhatsAppException {
        
        String message = composeBidNotificationMessage(bidNumber, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, String.valueOf(bidId), "BID");
    }
    
    /**
     * Send notification for deal event
     */
    public WhatsAppMessageResponse notifyDealEvent(String phoneNumber, String dealNumber,
                                                    String commodityName, String eventType,
                                                    String additionalInfo, Long dealId) 
            throws WhatsAppException {
        
        String message = composeDealNotificationMessage(dealNumber, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, String.valueOf(dealId), "DEAL");
    }

    /**
     * Send notification for deal event
     */
    public WhatsAppMessageResponse notifyDealEvent(String phoneNumber, String dealId,
                                                    String commodityName, String eventType,
                                                    String additionalInfo) 
            throws WhatsAppException {
        
        String message = composeDealNotificationMessage(dealId, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, dealId, "DEAL");
    }
    
    /**
     * Send notification for supply event
     */
    public WhatsAppMessageResponse notifySupplyEvent(String phoneNumber, String supplyNumber,
                                                      String commodityName, String eventType,
                                                      String additionalInfo, Long supplyId) 
            throws WhatsAppException {
        
        String message = composeSupplyNotificationMessage(supplyNumber, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, String.valueOf(supplyId), "SUPPLY");
    }

    /**
     * Send notification for supply event
     */
    public WhatsAppMessageResponse notifySupplyEvent(String phoneNumber, String supplyId,
                                                      String commodityName, String eventType,
                                                      String additionalInfo) 
            throws WhatsAppException {
        
        String message = composeSupplyNotificationMessage(supplyId, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, supplyId, "SUPPLY");
    }
    
    /**
     * Send notification for demand event
     */
    public WhatsAppMessageResponse notifyDemandEvent(String phoneNumber, String demandNumber,
                                                      String commodityName, String eventType,
                                                      String additionalInfo, Long demandId) 
            throws WhatsAppException {
        
        String message = composeDemandNotificationMessage(demandNumber, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, String.valueOf(demandId), "DEMAND");
    }

    /**
     * Send notification for demand event
     */
    public WhatsAppMessageResponse notifyDemandEvent(String phoneNumber, String demandId,
                                                      String commodityName, String eventType,
                                                      String additionalInfo) 
            throws WhatsAppException {
        
        String message = composeDemandNotificationMessage(demandId, commodityName, eventType, additionalInfo);
        return sendMessage(phoneNumber, message, demandId, "DEMAND");
    }
    
    // ==================== MESSAGE COMPOSITION ====================
    
    /**
     * Compose bid notification message
     */
    private String composeBidNotificationMessage(String bidNumber, String commodityName,
                                                  String eventType, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        message.append("🔔 *K.K.CANVANSSING AGENT Bid Notification*\n\n");
        message.append("*Bid Number:* ").append(bidNumber).append("\n");
        message.append("*Commodity:* ").append(commodityName).append("\n");
        message.append("*Event:* ").append(eventType).append("\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            message.append("*Details:* ").append(additionalInfo).append("\n");
        }
        
        message.append("\nPlease log in to https://kkcanvassing.com/party-login for more details.");
        return message.toString();
    }
    
    /**
     * Compose deal notification message
     */
    private String composeDealNotificationMessage(String dealId, String commodityName,
                                                   String eventType, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        message.append("🤝 *K.K.CANVANSSING AGENT Deal Notification*\n\n");
        message.append("*Deal ID:* ").append(dealId).append("\n");
        message.append("*Commodity:* ").append(commodityName).append("\n");
        message.append("*Event:* ").append(eventType).append("\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            message.append("*Details:* ").append(additionalInfo).append("\n");
        }
        
        message.append("\nPlease log in to https://kkcanvassing.com/party-login for more details.");
        return message.toString();
    }
    
    /**
     * Compose supply notification message
     */
    private String composeSupplyNotificationMessage(String supplyId, String commodityName,
                                                     String eventType, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        message.append("📦 *K.K.CANVANSSING AGENT Supply Notification*\n\n");
        message.append("*Supply ID:* ").append(supplyId).append("\n");
        message.append("*Commodity:* ").append(commodityName).append("\n");
        message.append("*Event:* ").append(eventType).append("\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            message.append("*Details:* ").append(additionalInfo).append("\n");
        }
        
        message.append("\nPlease log in to https://kkcanvassing.com/party-login for more details.");
        return message.toString();
    }
    
    /**
     * Compose demand notification message
     */
    private String composeDemandNotificationMessage(String demandId, String commodityName,
                                                     String eventType, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        message.append("📋 *K.K.CANVANSSING AGENT Demand Notification*\n\n");
        message.append("*Demand ID:* ").append(demandId).append("\n");
        message.append("*Commodity:* ").append(commodityName).append("\n");
        message.append("*Event:* ").append(eventType).append("\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            message.append("*Details:* ").append(additionalInfo).append("\n");
        }
        
        message.append("\nPlease log in to https://kkcanvassing.com/party-login for more details.");
        return message.toString();
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Validate message inputs
     */
    private void validateMessage(String phoneNumber, String message) 
            throws WhatsAppException {
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new WhatsAppException("Phone number is required", "INVALID_PHONE");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new WhatsAppException("Message content is required", "INVALID_MESSAGE");
        }
        
        if (message.length() > 4096) {
            throw new WhatsAppException("Message exceeds maximum length (4096 characters)", "MESSAGE_TOO_LONG");
        }
    }
    
    /**
     * Validate media URL
     */
    private void validateMediaUrl(String mediaUrl, String mediaType) 
            throws WhatsAppException {
        
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            throw new WhatsAppException("Media URL is required", "INVALID_MEDIA_URL");
        }
        
        if (mediaType == null || mediaType.trim().isEmpty()) {
            throw new WhatsAppException("Media type is required", "INVALID_MEDIA_TYPE");
        }
        
        // Validate media type
        String validTypes = "IMAGE,VIDEO,AUDIO,DOCUMENT";
        if (!validTypes.contains(mediaType.toUpperCase())) {
            throw new WhatsAppException("Invalid media type. Valid types: " + validTypes, "INVALID_MEDIA_TYPE");
        }
    }
    
    /**
     * Create disabled response
     */
    private WhatsAppMessageResponse createDisabledResponse() {
        return WhatsAppMessageResponse.builder()
                .success(false)
                .message("WhatsApp messaging is disabled")
                .status("DISABLED")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}
