package com.kalibyte.tradex.notification.listener;

import com.kalibyte.tradex.notification.config.WhatsAppConfig;
import com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos;
import com.kalibyte.tradex.notification.event.TradexNotificationEvents;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * NotificationEventListener - Asynchronously dispatches WhatsApp notifications
 * outside of core database transactions.
 * Uses AFTER_COMMIT to ensure notifications are only sent after successful DB commit,
 * preventing external network calls from stalling DB transactions or holding locks.
 * 
 * Also dispatches a real-time activity alert to Admin WhatsApp (+91-9321544177)
 * for each and every trading desk activity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final WhatsAppService whatsAppService;
    private final WhatsAppConfig whatsAppConfig;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    /**
     * Send immediate WhatsApp alert to Admin mobile number (default 9321544177)
     * for every activity occurring across the platform.
     */
    private void sendAdminAlert(String activityName, String activityDetails) {
        try {
            String adminPhone = whatsAppConfig.getAdminPhone();
            if (adminPhone == null || adminPhone.trim().isEmpty()) {
                adminPhone = "9321544177";
            }

            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String message = String.format(
                    "📢 *K.K.CANVANSSING AGENT | ADMIN ALERT*\n\n" +
                    "*ACTIVITY:* %s\n" +
                    "%s\n" +
                    "⏰ *TIME:* %s\n\n" +
                    "👉 *LOGIN DESK:* https://kkcanvassing.com/admin/login",
                    activityName != null ? activityName.toUpperCase() : "ACTIVITY",
                    activityDetails != null && !activityDetails.isEmpty() ? activityDetails.toUpperCase() + "\n" : "",
                    timestamp
            );

            whatsAppService.sendMessage(adminPhone, message, null, "ADMIN_ALERT");
            log.info("[Async WhatsApp] Admin alert sent to {} for: {}", adminPhone, activityName);
        } catch (Exception e) {
            log.warn("[Async WhatsApp] Admin alert notification skipped/failed (non-blocking): {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPartyActivation(TradexNotificationEvents.PartyActivationEvent event) {
        try {
            if (event.getPhone() != null && !event.getPhone().trim().isEmpty()) {
                whatsAppService.sendPartyActivation(event.getPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Party activation sent to {}", event.getPhone());
            }
            sendAdminAlert("PARTY ACTIVATION / REGISTRATION", 
                    "Party: " + (event.getTemplate() != null ? event.getTemplate().getPartyName() : "Unknown") + 
                    " | Mobile: " + event.getPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send party activation to {}: {}", event.getPhone(), e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNewListing(TradexNotificationEvents.NewListingEvent event) {
        try {
            if (event.getPhone() != null && !event.getPhone().trim().isEmpty()) {
                whatsAppService.sendNewListing(event.getPhone(), event.getTemplate());
                log.info("[Async WhatsApp] New listing alert sent to {}", event.getPhone());
            }
            sendAdminAlert("RATE LISTING POSTED", 
                    "Commodity: " + (event.getTemplate() != null ? event.getTemplate().getCommodity() : "") + 
                    " | Rate: ₹" + (event.getTemplate() != null ? event.getTemplate().getRate() : ""));
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send new listing alert to {}: {}", event.getPhone(), e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBidPlaced(TradexNotificationEvents.BidPlacedEvent event) {
        try {
            // Buyer confirmation
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendBidSubmitted(event.getBuyerPhone(), event.getBuyerTemplate());
                log.info("[Async WhatsApp] Bid confirmation sent to buyer {}", event.getBuyerPhone());
            }
            // Seller alert
            if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                whatsAppService.sendBidReceived(event.getSellerPhone(), event.getSellerTemplate());
                log.info("[Async WhatsApp] Bid alert sent to seller {}", event.getSellerPhone());
            }
            sendAdminAlert("BID PLACED", 
                    "Bid ID: " + (event.getBuyerTemplate() != null ? event.getBuyerTemplate().getBidId() : "") + 
                    " | Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send bid notifications: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCounterOffer(TradexNotificationEvents.CounterOfferEvent event) {
        try {
            if (event.getBidderPhone() != null && !event.getBidderPhone().trim().isEmpty()) {
                whatsAppService.sendCounterOffer(event.getBidderPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Counter offer alert sent to bidder {}", event.getBidderPhone());
            }
            sendAdminAlert("COUNTER OFFER SUBMITTED", "Bidder: " + event.getBidderPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send counter offer notification: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDealMatched(TradexNotificationEvents.DealMatchedEvent event) {
        try {
            // Buyer notification
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendDealAgreed(event.getBuyerPhone(), event.getBuyerTemplate());
                log.info("[Async WhatsApp] Deal agreed sent to buyer {}", event.getBuyerPhone());
            }
            // Seller notification
            if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                whatsAppService.sendDealConfirmed(event.getSellerPhone(), event.getSellerTemplate());
                log.info("[Async WhatsApp] Deal confirmed sent to seller {}", event.getSellerPhone());
            }
            sendAdminAlert("DEAL AGREED & MATCHED", 
                    "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send deal matched notifications: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDealLocked(TradexNotificationEvents.DealLockedEvent event) {
        try {
            // Buyer
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendQuantityLocked(event.getBuyerPhone(), event.getLockTemplate());
                TradexTemplateDtos.ContractReadyTemplate bTmpl = event.getBuyerContractTemplate() != null ? 
                        event.getBuyerContractTemplate() : event.getContractTemplate();
                whatsAppService.sendContractReady(event.getBuyerPhone(), bTmpl);
            }
            // Seller
            if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                whatsAppService.sendQuantityLocked(event.getSellerPhone(), event.getLockTemplate());
                TradexTemplateDtos.ContractReadyTemplate sTmpl = event.getSellerContractTemplate() != null ? 
                        event.getSellerContractTemplate() : event.getContractTemplate();
                whatsAppService.sendContractReady(event.getSellerPhone(), sTmpl);
            }
            log.info("[Async WhatsApp] Quantity locked & contract ready templates sent to counterparties");
            sendAdminAlert("DEAL QUANTITY LOCKED", 
                    "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send deal locked notifications: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onContractSigned(TradexNotificationEvents.ContractSignedEvent event) {
        try {
            if (event.isCompleted()) {
                if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                    whatsAppService.sendContractLocked(event.getBuyerPhone(), event.getLockedTemplate());
                }
                if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                    whatsAppService.sendContractLocked(event.getSellerPhone(), event.getLockedTemplate());
                }
                log.info("[Async WhatsApp] Contract locked final template sent to counterparties");
                sendAdminAlert("CONTRACT COMPLETED & LOCKED", 
                        "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
            } else {
                if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                    whatsAppService.sendContractApproved(event.getBuyerPhone(), event.getApprovedTemplate());
                }
                if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                    whatsAppService.sendContractApproved(event.getSellerPhone(), event.getApprovedTemplate());
                }
                log.info("[Async WhatsApp] Contract approved template sent to counterparties");
                sendAdminAlert("CONTRACT APPROVED", 
                        "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
            }
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send contract signed notifications: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBidAccepted(TradexNotificationEvents.BidAcceptedEvent event) {
        try {
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendBidAccepted(event.getBuyerPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Bid accepted notification sent to buyer {}", event.getBuyerPhone());
            }
            sendAdminAlert("BID ACCEPTED", "Buyer: " + event.getBuyerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send bid accepted notification: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBidRejected(TradexNotificationEvents.BidRejectedEvent event) {
        try {
            if (event.getBidderPhone() != null && !event.getBidderPhone().trim().isEmpty()) {
                whatsAppService.sendBidRejected(event.getBidderPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Bid rejected notification sent to bidder {}", event.getBidderPhone());
            }
            sendAdminAlert("BID REJECTED", "Bidder: " + event.getBidderPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send bid rejected notification: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNegotiationRejected(TradexNotificationEvents.NegotiationRejectedEvent event) {
        try {
            if (event.getCounterpartyPhone() != null && !event.getCounterpartyPhone().trim().isEmpty()) {
                whatsAppService.sendNegotiationRejected(event.getCounterpartyPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Negotiation rejected notification sent to {}", event.getCounterpartyPhone());
            }
            sendAdminAlert("NEGOTIATION REJECTED", "Counterparty: " + event.getCounterpartyPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send negotiation rejected notification: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDealCancelled(TradexNotificationEvents.DealCancelledEvent event) {
        try {
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendDealCancelled(event.getBuyerPhone(), event.getTemplate());
            }
            if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                whatsAppService.sendDealCancelled(event.getSellerPhone(), event.getTemplate());
            }
            log.info("[Async WhatsApp] Deal cancelled notifications dispatched");
            sendAdminAlert("DEAL CANCELLED", 
                    "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send deal cancelled notifications: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDispatchUpdate(TradexNotificationEvents.DispatchUpdateEvent event) {
        try {
            if (event.getRecipientPhone() != null && !event.getRecipientPhone().trim().isEmpty()) {
                whatsAppService.sendDispatchUpdate(event.getRecipientPhone(), event.getTemplate());
                log.info("[Async WhatsApp] Dispatch update sent to {}", event.getRecipientPhone());
            }
            sendAdminAlert("DISPATCH UPDATE", "Recipient: " + event.getRecipientPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send dispatch update notification: {}", e.getMessage());
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaymentUpdate(TradexNotificationEvents.PaymentUpdateEvent event) {
        try {
            if (event.getBuyerPhone() != null && !event.getBuyerPhone().trim().isEmpty()) {
                whatsAppService.sendPaymentUpdate(event.getBuyerPhone(), event.getTemplate());
            }
            if (event.getSellerPhone() != null && !event.getSellerPhone().trim().isEmpty()) {
                whatsAppService.sendPaymentUpdate(event.getSellerPhone(), event.getTemplate());
            }
            log.info("[Async WhatsApp] Payment settlement updates dispatched");
            sendAdminAlert("PAYMENT UPDATE", 
                    "Buyer: " + event.getBuyerPhone() + " | Seller: " + event.getSellerPhone());
        } catch (Exception e) {
            log.error("[Async WhatsApp] Failed to send payment update notifications: {}", e.getMessage());
        }
    }
}
