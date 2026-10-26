package com.kalibyte.tradex.notification.event;

import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Events for asynchronous WhatsApp notification dispatching.
 */
public class TradexNotificationEvents {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PartyActivationEvent {
        private String phone;
        private TradexTemplateDtos.PartyActivationTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NewListingEvent {
        private String phone;
        private TradexTemplateDtos.NewListingTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BidPlacedEvent {
        private String buyerPhone;
        private TradexTemplateDtos.BidSubmittedTemplate buyerTemplate;
        private String sellerPhone;
        private TradexTemplateDtos.BidReceivedTemplate sellerTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CounterOfferEvent {
        private String bidderPhone;
        private TradexTemplateDtos.CounterOfferTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DealMatchedEvent {
        private String buyerPhone;
        private TradexTemplateDtos.DealAgreedTemplate buyerTemplate;
        private String sellerPhone;
        private TradexTemplateDtos.DealConfirmedTemplate sellerTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DealLockedEvent {
        private String buyerPhone;
        private String sellerPhone;
        private TradexTemplateDtos.QuantityLockedTemplate lockTemplate;
        private TradexTemplateDtos.ContractReadyTemplate contractTemplate;
        private TradexTemplateDtos.ContractReadyTemplate buyerContractTemplate;
        private TradexTemplateDtos.ContractReadyTemplate sellerContractTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ContractSignedEvent {
        private String buyerPhone;
        private String sellerPhone;
        private boolean completed;
        private TradexTemplateDtos.ContractApprovedTemplate approvedTemplate;
        private TradexTemplateDtos.ContractLockedTemplate lockedTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DemandCreatedEvent {
        private String buyerPhone;
        private TradexTemplateDtos.DemandCreatedTemplate buyerTemplate;
        private java.util.List<String> sellerPhones;
        private TradexTemplateDtos.DemandAlertTemplate alertTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BidAcceptedEvent {
        private String buyerPhone;
        private TradexTemplateDtos.BidAcceptedTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BidRejectedEvent {
        private String bidderPhone;
        private TradexTemplateDtos.BidRejectedTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NegotiationRejectedEvent {
        private String counterpartyPhone;
        private TradexTemplateDtos.NegotiationRejectedTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DealCancelledEvent {
        private String buyerPhone;
        private String sellerPhone;
        private TradexTemplateDtos.DealCancelledTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DispatchUpdateEvent {
        private String recipientPhone;
        private TradexTemplateDtos.DispatchUpdateTemplate template;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaymentUpdateEvent {
        private String buyerPhone;
        private String sellerPhone;
        private TradexTemplateDtos.PaymentUpdateTemplate template;
    }
}
