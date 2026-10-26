package com.kalibyte.tradex.notification.dto.template;

import lombok.Getter;

/**
 * WhatsApp Template Types for TradeX
 */
@Getter
public enum WhatsAppTemplateType {
    
    PARTY_ACTIVATION("tradex_party_activation", "Party Activation Link"),
    NEW_LISTING("tradex_new_listing", "New Market Listing Alert"),
    RATE_UPDATED("tradex_rate_updated", "Commodity Rate Updated"),
    BID_SUBMITTED("tradex_bid_submitted", "Bid Confirmation for Buyer"),
    BID_RECEIVED("tradex_bid_received", "Bid Alert for Seller"),
    COUNTER_OFFER("tradex_counter_offer", "Negotiation Counter Offer"),
    DEAL_AGREED("tradex_deal_agreed", "Deal Agreed Notification"),
    DEAL_CONFIRMED("tradex_deal_confirmed", "Deal Confirmed Notification"),
    QUANTITY_LOCKED("tradex_quantity_locked", "Quantity Locked against Deal"),
    CONTRACT_READY("tradex_contract_ready", "Contract Ready for Review"),
    CONTRACT_APPROVED("tradex_contract_approved", "Contract Approved"),
    CONTRACT_LOCKED("tradex_contract_locked", "Contract Finalized & Locked"),
    
    // Additional Action Templates
    DEMAND_CREATED("tradex_demand_created", "Buyer Demand Requirement Confirmation"),
    DEMAND_ALERT("tradex_demand_alert", "Seller Demand Opportunity Alert"),
    BID_ACCEPTED("tradex_bid_accepted", "Bid Accepted Confirmation"),
    BID_REJECTED("tradex_bid_rejected", "Bid Rejected Notification"),
    NEGOTIATION_REJECTED("tradex_negotiation_rejected", "Negotiation Ended Alert"),
    DEAL_CANCELLED("tradex_deal_cancelled", "Deal Cancelled Notification"),
    DISPATCH_UPDATE("tradex_dispatch_update", "Dispatch & Logistics Update"),
    PAYMENT_UPDATE("tradex_payment_update", "Payment & Settlement Update");

    private final String templateName;
    private final String description;

    WhatsAppTemplateType(String templateName, String description) {
        this.templateName = templateName;
        this.description = description;
    }
}
