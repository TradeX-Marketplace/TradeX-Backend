package com.kalibyte.tradex.notification.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Strongly-typed Template DTOs for all 12 TradeX WhatsApp Templates
 * Supports both Cloud API (params list) and Web Engine (formatted text).
 */
public class TradexTemplateDtos {

    /**
     * 1. Party Activation
     * Template name: tradex_party_activation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartyActivationTemplate {
        private String partyName;
        private String secureLink;
        @Builder.Default
        private String businessName = "K.K.CANVANSSING AGENT";
        private String primaryMobile;

        public List<String> toParams() {
            return Arrays.asList(
                partyName != null ? partyName : "",
                secureLink != null ? secureLink : "https://kkcanvassing.com/party-login",
                businessName != null ? businessName : "K.K.CANVANSSING AGENT"
            );
        }

        public String toFormattedText() {
            String pMobile = primaryMobile != null && !primaryMobile.trim().isEmpty() ? primaryMobile.trim() : "";
            return String.format(
                "*K.K.CANVANSSING AGENT | REGISTRATION CONFIRMATION*\n\n" +
                "WELCOME %s,\n\n" +
                "YOUR K.K.CANVANSSING AGENT ACCOUNT HAS BEEN CREATED.\n\n" +
                "👉 PARTY LOGIN LINK:\n" +
                "%s\n\n" +
                "LOGIN CREDENTIALS:\n" +
                "MOBILE NUMBER AS PASSWORD: %s\n\n" +
                "REGARDS,\n" +
                "%s",
                (partyName != null ? partyName : "USER").toUpperCase(),
                (secureLink != null ? secureLink : "https://kkcanvassing.com/party-login"),
                (!pMobile.isEmpty() ? pMobile : "REGISTERED MOBILE NUMBER"),
                (businessName != null ? businessName : "K.K.CANVANSSING AGENT").toUpperCase()
            );
        }
    }

    /**
     * 2. New Market Listing
     * Template name: tradex_new_listing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewListingTemplate {
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String rate;
        private String location;
        private String availableFrom;
        private String listingLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                rate != null ? rate : "",
                location != null ? location : "",
                availableFrom != null ? availableFrom : "",
                listingLink != null ? listingLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT MARKET ALERT\n\n" +
                "%s is available in the market.\n\n" +
                "Quantity: %s %s\n" +
                "Rate: ₹%s/%s\n" +
                "Location: %s\n" +
                "Available From: %s\n\n" +
                "View the listing and place your bid:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                rate != null ? rate : "",
                unit != null ? unit : "MT",
                location != null ? location : "",
                availableFrom != null ? availableFrom : "",
                listingLink != null ? listingLink : ""
            );
        }
    }

    /**
     * 3. Commodity Rate Updated
     * Template name: tradex_rate_updated
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RateUpdatedTemplate {
        private String commodity;
        private String currentRate;
        @Builder.Default
        private String unit = "MT";
        private String availableQuantity;
        private String location;
        private String marketLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                currentRate != null ? currentRate : "",
                unit != null ? unit : "MT",
                availableQuantity != null ? availableQuantity : "",
                location != null ? location : "",
                marketLink != null ? marketLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT RATE UPDATE\n\n" +
                "Commodity: %s\n" +
                "Current Rate: ₹%s/%s\n" +
                "Available Quantity: %s %s\n" +
                "Location: %s\n\n" +
                "View the latest market details:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                currentRate != null ? currentRate : "",
                unit != null ? unit : "MT",
                availableQuantity != null ? availableQuantity : "",
                unit != null ? unit : "MT",
                location != null ? location : "",
                marketLink != null ? marketLink : ""
            );
        }
    }

    /**
     * 4. Bid Submitted (Buyer)
     * Template name: tradex_bid_submitted
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BidSubmittedTemplate {
        private String commodity;
        private String bidQuantity;
        @Builder.Default
        private String unit = "MT";
        private String bidRate;
        private String bidId;
        private String bidLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                bidQuantity != null ? bidQuantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                bidId != null ? bidId : "",
                bidLink != null ? bidLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT BID CONFIRMATION\n\n" +
                "Your bid has been submitted successfully.\n\n" +
                "Commodity: %s\n" +
                "Bid Quantity: %s %s\n" +
                "Bid Rate: ₹%s/%s\n" +
                "Bid ID: %s\n\n" +
                "You can view the bid status here:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                bidQuantity != null ? bidQuantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                unit != null ? unit : "MT",
                bidId != null ? bidId : "",
                bidLink != null ? bidLink : ""
            );
        }
    }

    /**
     * 5. Bid Received (Seller)
     * Template name: tradex_bid_received
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BidReceivedTemplate {
        private String commodity;
        private String bidQuantity;
        @Builder.Default
        private String unit = "MT";
        private String bidRate;
        private String bidId;
        private String reviewLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                bidQuantity != null ? bidQuantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                bidId != null ? bidId : "",
                reviewLink != null ? reviewLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT BID ALERT\n\n" +
                "A new bid has been received for your listing.\n\n" +
                "Commodity: %s\n" +
                "Bid Quantity: %s %s\n" +
                "Bid Rate: ₹%s/%s\n" +
                "Bid ID: %s\n\n" +
                "Review the bid and continue negotiation:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                bidQuantity != null ? bidQuantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                unit != null ? unit : "MT",
                bidId != null ? bidId : "",
                reviewLink != null ? reviewLink : ""
            );
        }
    }

    /**
     * 6. Counter Offer
     * Template name: tradex_counter_offer
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CounterOfferTemplate {
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String counterRate;
        private String negotiationLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                counterRate != null ? counterRate : "",
                negotiationLink != null ? negotiationLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT NEGOTIATION UPDATE\n\n" +
                "A counter offer has been submitted for %s.\n\n" +
                "Quantity: %s %s\n" +
                "Counter Rate: ₹%s/%s\n\n" +
                "Review the negotiation and respond:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                counterRate != null ? counterRate : "",
                unit != null ? unit : "MT",
                negotiationLink != null ? negotiationLink : ""
            );
        }
    }

    /**
     * 7. Deal Agreed
     * Template name: tradex_deal_agreed
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DealAgreedTemplate {
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String finalRate;
        private String dealId;
        private String dealLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT DEAL UPDATE\n\n" +
                "A deal has been agreed for %s.\n\n" +
                "Quantity: %s %s\n" +
                "Final Rate: ₹%s/%s\n" +
                "Deal ID: %s\n\n" +
                "Please review the deal details:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                unit != null ? unit : "MT",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }
    }

    /**
     * 8. Deal Confirmed
     * Template name: tradex_deal_confirmed
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DealConfirmedTemplate {
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String finalRate;
        private String dealId;
        private String dealLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT DEAL CONFIRMED\n\n" +
                "Your deal has been confirmed.\n\n" +
                "Commodity: %s\n" +
                "Quantity: %s %s\n" +
                "Final Rate: ₹%s/%s\n" +
                "Deal ID: %s\n\n" +
                "View deal details:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                unit != null ? unit : "MT",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }
    }

    /**
     * 9. Quantity Locked
     * Template name: tradex_quantity_locked
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuantityLockedTemplate {
        private String dealId;
        private String commodity;
        private String lockedQuantity;
        @Builder.Default
        private String unit = "MT";
        private String remainingQuantity;
        private String transactionLink;

        public List<String> toParams() {
            return Arrays.asList(
                dealId != null ? dealId : "",
                commodity != null ? commodity : "",
                lockedQuantity != null ? lockedQuantity : "",
                unit != null ? unit : "MT",
                remainingQuantity != null ? remainingQuantity : "",
                transactionLink != null ? transactionLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT QUANTITY UPDATE\n\n" +
                "Quantity has been locked against Deal %s.\n\n" +
                "Commodity: %s\n" +
                "Locked Quantity: %s %s\n" +
                "Remaining Quantity: %s %s\n\n" +
                "View transaction details:\n\n" +
                "%s",
                dealId != null ? dealId : "",
                commodity != null ? commodity : "",
                lockedQuantity != null ? lockedQuantity : "",
                unit != null ? unit : "MT",
                remainingQuantity != null ? remainingQuantity : "",
                unit != null ? unit : "MT",
                transactionLink != null ? transactionLink : ""
            );
        }
    }

    /**
     * 10. Contract Ready
     * Template name: tradex_contract_ready
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractReadyTemplate {
        private String contractNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String finalRate;
        private String contractLink;
        private String counterpartyRole;
        private String counterpartyName;
        private String counterpartyPhone;
        private String counterpartyLocation;

        public List<String> toParams() {
            return Arrays.asList(
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                contractLink != null ? contractLink : ""
            );
        }

        public String toFormattedText() {
            StringBuilder sb = new StringBuilder();
            sb.append("K.K.CANVANSSING AGENT - DEAL APPROVED & CONTRACT READY\n\n");
            sb.append(String.format("Contract %s has been generated for your confirmed deal.\n\n",
                contractNumber != null ? contractNumber : ""));
            sb.append(String.format("Commodity: %s\nQuantity: %s %s\nFinal Rate: ₹%s/%s\n\n",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                unit != null ? unit : "MT"));
            if (counterpartyName != null && !counterpartyName.trim().isEmpty()) {
                sb.append(String.format("%s Details Revealed:\nCompany: %s\nMobile: %s\nLocation: %s\n\n",
                    counterpartyRole != null ? counterpartyRole : "Counterparty",
                    counterpartyName,
                    counterpartyPhone != null ? counterpartyPhone : "-",
                    counterpartyLocation != null ? counterpartyLocation : "-"));
            }
            sb.append("Review/Download Contract PDF:\n");
            sb.append(contractLink != null ? contractLink : "https://kkcanvassing.com/party-login");
            return sb.toString();
        }
    }

    /**
     * 11. Contract Approved
     * Template name: tradex_contract_approved
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractApprovedTemplate {
        private String contractNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String finalRate;
        private String contractLink;

        public List<String> toParams() {
            return Arrays.asList(
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                contractLink != null ? contractLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT CONTRACT UPDATE\n\n" +
                "Contract %s has been approved.\n\n" +
                "Commodity: %s\n" +
                "Quantity: %s %s\n" +
                "Final Rate: ₹%s/%s\n\n" +
                "View the approved contract:\n\n" +
                "%s",
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                unit != null ? unit : "MT",
                contractLink != null ? contractLink : ""
            );
        }
    }

    /**
     * 12. Contract Locked
     * Template name: tradex_contract_locked
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractLockedTemplate {
        private String contractNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String finalRate;
        private String contractLink;

        public List<String> toParams() {
            return Arrays.asList(
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                contractLink != null ? contractLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT CONTRACT LOCKED\n\n" +
                "Contract %s has been finalized and locked.\n\n" +
                "Commodity: %s\n" +
                "Quantity: %s %s\n" +
                "Final Rate: ₹%s/%s\n\n" +
                "View contract:\n\n" +
                "%s",
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                finalRate != null ? finalRate : "",
                unit != null ? unit : "MT",
                contractLink != null ? contractLink : ""
            );
        }
    }

    /**
     * 13. Demand Created (Buyer Confirmation)
     * Template name: tradex_demand_created
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DemandCreatedTemplate {
        private String demandNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String targetRate;
        private String destination;
        private String validTill;
        private String demandLink;

        public List<String> toParams() {
            return Arrays.asList(
                demandNumber != null ? demandNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                targetRate != null ? targetRate : "",
                destination != null ? destination : "",
                validTill != null ? validTill : "",
                demandLink != null ? demandLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT PROCUREMENT REQUIREMENT POSTED\n\n" +
                "Your demand requirement %s has been posted to market.\n\n" +
                "Commodity: %s\n" +
                "Required Quantity: %s %s\n" +
                "Target Rate: ₹%s/%s\n" +
                "Destination: %s\n" +
                "Valid Till: %s\n\n" +
                "Manage your requirement:\n\n" +
                "%s",
                demandNumber != null ? demandNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                targetRate != null ? targetRate : "",
                unit != null ? unit : "MT",
                destination != null ? destination : "Unspecified",
                validTill != null ? validTill : "Open",
                demandLink != null ? demandLink : ""
            );
        }
    }

    /**
     * 14. Demand Opportunity Alert (Sent to matching Sellers)
     * Template name: tradex_demand_alert
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DemandAlertTemplate {
        private String commodity;
        private String requiredQuantity;
        @Builder.Default
        private String unit = "MT";
        private String targetRate;
        private String destinationZone;
        private String demandId;
        private String quoteLink;

        public List<String> toParams() {
            return Arrays.asList(
                commodity != null ? commodity : "",
                requiredQuantity != null ? requiredQuantity : "",
                unit != null ? unit : "MT",
                targetRate != null ? targetRate : "",
                destinationZone != null ? destinationZone : "",
                demandId != null ? demandId : "",
                quoteLink != null ? quoteLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT DEMAND OPPORTUNITY ALERT\n\n" +
                "A buyer has posted an urgent requirement for %s.\n\n" +
                "Required Quantity: %s %s\n" +
                "Target Rate: ₹%s/%s\n" +
                "Delivery Zone: %s\n" +
                "Demand Ref: %s\n\n" +
                "Supply your lot or submit your quote:\n\n" +
                "%s",
                commodity != null ? commodity : "",
                requiredQuantity != null ? requiredQuantity : "",
                unit != null ? unit : "MT",
                targetRate != null ? targetRate : "",
                unit != null ? unit : "MT",
                destinationZone != null ? destinationZone : "All Zones",
                demandId != null ? demandId : "",
                quoteLink != null ? quoteLink : ""
            );
        }
    }

    /**
     * 15. Bid Accepted (Buyer Notification)
     * Template name: tradex_bid_accepted
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BidAcceptedTemplate {
        private String bidNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String agreedRate;
        private String dealId;
        private String dealLink;

        public List<String> toParams() {
            return Arrays.asList(
                bidNumber != null ? bidNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                agreedRate != null ? agreedRate : "",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT BID ACCEPTED\n\n" +
                "Congratulations! Your bid %s has been accepted by the counterparty.\n\n" +
                "Commodity: %s\n" +
                "Quantity: %s %s\n" +
                "Agreed Rate: ₹%s/%s\n" +
                "Deal Reference: %s\n\n" +
                "Review the deal and proceed to contract:\n\n" +
                "%s",
                bidNumber != null ? bidNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                agreedRate != null ? agreedRate : "",
                unit != null ? unit : "MT",
                dealId != null ? dealId : "",
                dealLink != null ? dealLink : ""
            );
        }
    }

    /**
     * 16. Bid Rejected (Bidder Notification)
     * Template name: tradex_bid_rejected
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BidRejectedTemplate {
        private String bidNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String bidRate;
        private String reason;
        private String marketplaceLink;

        public List<String> toParams() {
            return Arrays.asList(
                bidNumber != null ? bidNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                reason != null ? reason : "",
                marketplaceLink != null ? marketplaceLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT BID UPDATE\n\n" +
                "Your bid %s for %s was declined.\n\n" +
                "Quantity: %s %s\n" +
                "Bid Rate: ₹%s/%s\n" +
                "Reason: %s\n\n" +
                "Explore active marketplace listings or revise your bid:\n\n" +
                "%s",
                bidNumber != null ? bidNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                bidRate != null ? bidRate : "",
                unit != null ? unit : "MT",
                reason != null ? reason : "Rate mismatch with seller expectations",
                marketplaceLink != null ? marketplaceLink : ""
            );
        }
    }

    /**
     * 17. Negotiation Rejected / Terminated
     * Template name: tradex_negotiation_rejected
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NegotiationRejectedTemplate {
        private String negotiationCode;
        private String commodity;
        private String lastOffer;
        private String reason;
        private String marketplaceLink;

        public List<String> toParams() {
            return Arrays.asList(
                negotiationCode != null ? negotiationCode : "",
                commodity != null ? commodity : "",
                lastOffer != null ? lastOffer : "",
                reason != null ? reason : "",
                marketplaceLink != null ? marketplaceLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT NEGOTIATION CONCLUDED\n\n" +
                "Negotiation round %s for %s has been closed without agreement.\n\n" +
                "Last Offer: ₹%s\n" +
                "Reason: %s\n\n" +
                "Browse alternative supply lots or post new bids:\n\n" +
                "%s",
                negotiationCode != null ? negotiationCode : "",
                commodity != null ? commodity : "",
                lastOffer != null ? lastOffer : "N/A",
                reason != null ? reason : "Offer rejected by counterparty",
                marketplaceLink != null ? marketplaceLink : ""
            );
        }
    }

    /**
     * 18. Deal Cancelled / Lock Released
     * Template name: tradex_deal_cancelled
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DealCancelledTemplate {
        private String dealNumber;
        private String commodity;
        private String quantity;
        @Builder.Default
        private String unit = "MT";
        private String reason;
        private String supportLink;

        public List<String> toParams() {
            return Arrays.asList(
                dealNumber != null ? dealNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                reason != null ? reason : "",
                supportLink != null ? supportLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT DEAL CANCELLATION NOTICE\n\n" +
                "Deal %s for %s (%s %s) has been cancelled.\n\n" +
                "Reason: %s\n" +
                "Inventory status: Allocation released back to available market.\n\n" +
                "For inquiries, contact Support:\n\n" +
                "%s",
                dealNumber != null ? dealNumber : "",
                commodity != null ? commodity : "",
                quantity != null ? quantity : "",
                unit != null ? unit : "MT",
                reason != null ? reason : "Administrative cancellation or signing timeout",
                supportLink != null ? supportLink : ""
            );
        }
    }

    /**
     * 19. Dispatch & Logistics Update
     * Template name: tradex_dispatch_update
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DispatchUpdateTemplate {
        private String contractNumber;
        private String commodity;
        private String dispatchQuantity;
        @Builder.Default
        private String unit = "MT";
        private String vehicleNumber;
        private String driverContact;
        private String dispatchDate;
        private String trackingLink;

        public List<String> toParams() {
            return Arrays.asList(
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                dispatchQuantity != null ? dispatchQuantity : "",
                unit != null ? unit : "MT",
                vehicleNumber != null ? vehicleNumber : "",
                driverContact != null ? driverContact : "",
                dispatchDate != null ? dispatchDate : "",
                trackingLink != null ? trackingLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT DISPATCH & LOGISTICS DISPATCHED\n\n" +
                "Shipment for Contract %s has been dispatched.\n\n" +
                "Commodity: %s\n" +
                "Quantity: %s %s\n" +
                "Vehicle Number: %s\n" +
                "Driver Contact: %s\n" +
                "Dispatch Date: %s\n\n" +
                "Track consignment & upload weighbridge receipt:\n\n" +
                "%s",
                contractNumber != null ? contractNumber : "",
                commodity != null ? commodity : "",
                dispatchQuantity != null ? dispatchQuantity : "",
                unit != null ? unit : "MT",
                vehicleNumber != null ? vehicleNumber : "Assigned",
                driverContact != null ? driverContact : "On File",
                dispatchDate != null ? dispatchDate : "Today",
                trackingLink != null ? trackingLink : ""
            );
        }
    }

    /**
     * 20. Payment & Escrow Settlement Update
     * Template name: tradex_payment_update
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentUpdateTemplate {
        private String contractNumber;
        private String paymentStage;
        private String amount;
        private String transactionRef;
        private String status;
        private String receiptLink;

        public List<String> toParams() {
            return Arrays.asList(
                contractNumber != null ? contractNumber : "",
                paymentStage != null ? paymentStage : "",
                amount != null ? amount : "",
                transactionRef != null ? transactionRef : "",
                status != null ? status : "",
                receiptLink != null ? receiptLink : ""
            );
        }

        public String toFormattedText() {
            return String.format(
                "K.K.CANVANSSING AGENT SETTLEMENT & PAYMENT UPDATE\n\n" +
                "Payment transaction recorded for Contract %s.\n\n" +
                "Stage: %s\n" +
                "Amount: ₹%s\n" +
                "UTR / Ref No: %s\n" +
                "Status: %s\n\n" +
                "View payment advice & settlement ledger:\n\n" +
                "%s",
                contractNumber != null ? contractNumber : "",
                paymentStage != null ? paymentStage : "Escrow Deposit",
                amount != null ? amount : "",
                transactionRef != null ? transactionRef : "Pending Bank Acknowledgment",
                status != null ? status : "CONFIRMED",
                receiptLink != null ? receiptLink : ""
            );
        }
    }
}
