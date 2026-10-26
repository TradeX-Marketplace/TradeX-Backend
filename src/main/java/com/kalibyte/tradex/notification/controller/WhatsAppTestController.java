package com.kalibyte.tradex.notification.controller;

import com.kalibyte.tradex.notification.config.WhatsAppConfig;
import com.kalibyte.tradex.notification.dto.WhatsAppMessageResponse;
import com.kalibyte.tradex.notification.exception.WhatsAppException;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WhatsApp Test Controller
 * 
 * Endpoints for testing WhatsApp message sending functionality
 * 
 * ⚠️ NOTE: This controller should be disabled in production!
 * Remove or secure with appropriate authorization.
 */
@RestController
@RequestMapping("/api/v1/test/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppTestController {
    
    private final WhatsAppService whatsAppService;
    private final WhatsAppConfig whatsAppConfig;
    private final org.springframework.web.client.RestTemplate restTemplate;
    
    /**
     * Test 1: Send Simple Text Message
     * 
     * POST /api/v1/test/whatsapp/send-simple
     * 
     * Request Body:
     * {
     *   "phone": "+919876543210",
     *   "message": "Test message from TradeX"
     * }
     */
    @PostMapping("/send-simple")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> sendSimpleMessage(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String message,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        
        String targetPhone = (phone != null && !phone.trim().isEmpty()) ? phone : (body != null ? body.get("phone") : null);
        String targetMessage = (message != null && !message.trim().isEmpty()) ? message : (body != null ? body.get("message") : null);

        if (targetPhone == null || targetPhone.trim().isEmpty()) {
            targetPhone = whatsAppConfig.getFromNumber();
        }
        if (targetMessage == null || targetMessage.trim().isEmpty()) {
            targetMessage = "Test message from TradeX Exchange Desk";
        }

        log.info("=== TEST: Sending simple WhatsApp message ===");
        log.info("Phone: {}", targetPhone);
        log.info("Message: {}", targetMessage);
        
        try {
            WhatsAppMessageResponse response = whatsAppService.sendMessage(targetPhone, targetMessage);
            
            log.info("✅ Message sent successfully!");
            log.info("Response: {}", response);
            
            return ResponseEntity.ok(
                ApiResponse.success(response, "Message sent successfully")
            );
        } catch (WhatsAppException e) {
            log.error("❌ Failed to send message: {}", e.getMessage(), e);
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    "Failed to send message: " + e.getMessage(),
                    e.getErrorCode()
                ));
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "Unexpected error: " + e.getMessage(),
                    "INTERNAL_ERROR"
                ));
        }
    }
    
    /**
     * Test 2: Send Bid Notification
     * 
     * POST /api/v1/test/whatsapp/bid-notification
     * 
     * Request Parameters:
     * - phone: Recipient phone number
     * - bidNumber: Bid number
     * - commodityName: Commodity name
     * - bidRate: Bid rate
     * - quantity: Bid quantity
     */
    @PostMapping("/bid-notification")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> sendBidNotification(
            @RequestParam String phone,
            @RequestParam String bidNumber,
            @RequestParam String commodityName,
            @RequestParam String bidRate,
            @RequestParam String quantity) {
        
        log.info("=== TEST: Sending bid notification ===");
        log.info("Phone: {}", phone);
        log.info("Bid: {} | Commodity: {} | Rate: {} | Qty: {}", 
                 bidNumber, commodityName, bidRate, quantity);
        
        try {
            String details = "Rate: " + bidRate + " per unit | Quantity: " + quantity;
            
            WhatsAppMessageResponse response = whatsAppService.notifyBidEvent(
                phone,
                bidNumber,
                commodityName,
                "PLACED",
                details,
                1L
            );
            
            log.info("✅ Bid notification sent successfully!");
            log.info("Response: {}", response);
            
            return ResponseEntity.ok(
                ApiResponse.success(response, "Bid notification sent successfully")
            );
        } catch (WhatsAppException e) {
            log.error("❌ Failed to send bid notification: {}", e.getMessage(), e);
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    "Failed to send bid notification: " + e.getMessage(),
                    e.getErrorCode()
                ));
        }
    }
    
    /**
     * Test 3: Send Deal Notification
     * 
     * POST /api/v1/test/whatsapp/deal-notification
     * 
     * Request Parameters:
     * - phone: Recipient phone number
     * - dealId: Deal ID
     * - commodityName: Commodity name
     * - quantity: Deal quantity
     * - rate: Deal rate
     */
    @PostMapping("/deal-notification")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> sendDealNotification(
            @RequestParam String phone,
            @RequestParam String dealId,
            @RequestParam String commodityName,
            @RequestParam String quantity,
            @RequestParam String rate) {
        
        log.info("=== TEST: Sending deal notification ===");
        log.info("Phone: {}", phone);
        log.info("Deal: {} | Commodity: {} | Qty: {} | Rate: {}", 
                 dealId, commodityName, quantity, rate);
        
        try {
            String details = "Quantity: " + quantity + " at rate: " + rate;
            
            WhatsAppMessageResponse response = whatsAppService.notifyDealEvent(
                phone,
                dealId,
                commodityName,
                "MATCHED",
                details
            );
            
            log.info("✅ Deal notification sent successfully!");
            log.info("Response: {}", response);
            
            return ResponseEntity.ok(
                ApiResponse.success(response, "Deal notification sent successfully")
            );
        } catch (WhatsAppException e) {
            log.error("❌ Failed to send deal notification: {}", e.getMessage(), e);
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    "Failed to send deal notification: " + e.getMessage(),
                    e.getErrorCode()
                ));
        }
    }
    
    /**
     * Test 4: Send with Different Phone Formats
     * 
     * POST /api/v1/test/whatsapp/phone-formats
     * 
     * Tests phone number normalization with different formats
     */
    @PostMapping("/phone-formats")
    public ResponseEntity<ApiResponse<String>> testPhoneFormats() {
        
        log.info("=== TEST: Phone number normalization ===");
        
        String[] phoneFormats = {
            "9876543210",           // 10 digits
            "919876543210",         // With country code
            "+919876543210",        // International format
            "+91-987-654-3210"      // Formatted
        };
        
        StringBuilder results = new StringBuilder();
        results.append("Phone Format Testing:\n\n");
        
        for (String phone : phoneFormats) {
            log.info("Testing phone format: {}", phone);
            results.append("Input: ").append(phone).append(" → ");
            
            try {
                WhatsAppMessageResponse response = whatsAppService.sendMessage(
                    phone,
                    "Phone format test message"
                );
                results.append("✅ SUCCESS\n");
                log.info("✅ Format {} worked!", phone);
            } catch (WhatsAppException e) {
                results.append("❌ FAILED - ").append(e.getMessage()).append("\n");
                log.error("❌ Format {} failed: {}", phone, e.getMessage());
            }
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(results.toString(), "Phone format testing completed")
        );
    }
    
    /**
     * Test 5: Configuration Check
     * 
     * GET /api/v1/test/whatsapp/config
     * 
     * Returns current WhatsApp configuration (for debugging)
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<ConfigCheckResponse>> checkConfiguration() {
        
        log.info("=== TEST: Checking configuration ===");
        
        ConfigCheckResponse config = ConfigCheckResponse.builder()
                .fromNumber(maskPhoneNumber(whatsAppConfig.getFromNumber()))
                .enabled(whatsAppConfig.isEnabled())
                .apiKeyConfigured(whatsAppConfig.getApiKey() != null && !whatsAppConfig.getApiKey().isEmpty())
                .baseUrlConfigured(whatsAppConfig.getBaseUrl() != null && !whatsAppConfig.getBaseUrl().isEmpty())
                .timeoutSeconds(whatsAppConfig.getTimeoutSeconds())
                .retryAttempts(whatsAppConfig.getRetryAttempts())
                .dispatchDelayMs(whatsAppConfig.getDispatchDelayMs())
                .message("Configuration loaded successfully: Config phone = " + whatsAppConfig.getFromNumber())
                .build();
        
        log.info("Configuration Status: {}", config);
        
        return ResponseEntity.ok(
            ApiResponse.success(config, "Configuration check completed")
        );
    }
    
    /**
     * Test 6: Connection Test
     * 
     * GET /api/v1/test/whatsapp/connection
     * 
     * Tests connectivity to BookBey API
     */
    @GetMapping("/connection")
    public ResponseEntity<ApiResponse<String>> testConnection() {
        
        log.info("=== TEST: Testing API connection ===");
        
        try {
            // Try to send a test message to verify connection
            // This won't actually send but will test API connectivity
            WhatsAppMessageResponse response = whatsAppService.sendMessage(
                "+919876543210",
                "Connection test - this is a test message"
            );
            
            if (response.isSuccess()) {
                log.info("✅ Connection test successful!");
                return ResponseEntity.ok(
                    ApiResponse.success("Connected to BookBey API successfully", 
                                       "Connection verified")
                );
            } else {
                log.warn("⚠️ API responded but with error: {}", response.getErrorMessage());
                return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponse.error(
                        "API connection issue: " + response.getErrorMessage(),
                        response.getErrorCode()
                    ));
            }
        } catch (Exception e) {
            log.error("❌ Connection test failed: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                    "Failed to connect to BookBey API: " + e.getMessage(),
                    "CONNECTION_ERROR"
                ));
        }
    }
    
    /**
     * Test 7: Send to Your Phone
     * 
     * POST /api/v1/test/whatsapp/send-to-my-phone
     * 
     * Send test message to your phone
     */
    @PostMapping("/send-to-my-phone")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> sendToMyPhone(
            @RequestParam String phone) {
        
        log.info("=== TEST: Sending test message to your phone ===");
        log.info("Phone: {}", phone);
        
        try {
            String testMessage = "🧪 Test message from TradeX WhatsApp Integration\n\n" +
                               "If you receive this message, WhatsApp integration is working! ✅\n\n" +
                               "Sender: TradeX (+917303344177)\n" +
                               "Time: " + java.time.LocalDateTime.now();
            
            WhatsAppMessageResponse response = whatsAppService.sendMessage(
                phone,
                testMessage
            );
            
            log.info("✅ Test message sent!");
            log.info("Message ID: {}", response.getMessageId());
            log.info("Status: {}", response.getStatus());
            
            return ResponseEntity.ok(
                ApiResponse.success(response, "Test message sent to " + phone)
            );
        } catch (WhatsAppException e) {
            log.error("❌ Failed to send test message: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    "Failed to send test message: " + e.getMessage(),
                    e.getErrorCode()
                ));
        }
    }
    
    /**
     * Test 7: Send to Config Mobile Number
     * 
     * POST /api/v1/test/whatsapp/send-to-config-mobile
     * 
     * Sends test message directly to the mobile number configured in application.yaml
     */
    @PostMapping("/send-to-config-mobile")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> sendToConfigMobile(
            @RequestParam(required = false) String customMessage) {
        
        String targetPhone = whatsAppConfig.getFromNumber();
        log.info("=== TEST: Sending WhatsApp message to config mobile number: {} ===", targetPhone);
        
        try {
            String messageToSend = (customMessage != null && !customMessage.trim().isEmpty())
                    ? customMessage
                    : "🧪 Test message from TradeX WhatsApp Integration!\n\n" +
                      "Recipient: Configured Mobile Number (" + targetPhone + ")\n" +
                      "Status: Verified & Connected ✅\n" +
                      "Time: " + java.time.LocalDateTime.now();
            
            WhatsAppMessageResponse response = whatsAppService.sendMessage(
                targetPhone,
                messageToSend
            );
            
            log.info("✅ Message sent to config mobile number successfully!");
            return ResponseEntity.ok(
                ApiResponse.success(response, "Test message sent successfully to config mobile " + targetPhone)
            );
        } catch (WhatsAppException e) {
            log.error("❌ Failed to send message to config mobile: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    "Failed to send to config mobile: " + e.getMessage(),
                    e.getErrorCode()
                ));
        }
    }

    /**
     * Test 8: Check BookBey API Quota and Status
     * 
     * GET /api/v1/test/whatsapp/live-status
     * 
     * Calls BookBey status.php endpoint to verify API key & quota
     */
    @GetMapping("/live-status")
    public ResponseEntity<ApiResponse<Object>> checkBookBeyLiveStatus() {
        log.info("=== TEST: Checking BookBey Live Status & Quota ===");
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-API-Key", whatsAppConfig.getApiKey());
            headers.set("Authorization", "Bearer " + whatsAppConfig.getApiKey());
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    whatsAppConfig.getStatusEndpoint(),
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.ok(
                ApiResponse.success(response.getBody(), "BookBey live quota and status retrieved successfully")
            );
        } catch (Exception e) {
            log.error("❌ Failed to fetch live status: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("Failed to fetch BookBey status: " + e.getMessage(), "BOOKBEY_ERROR"));
        }
    }

    // ==================== TEMPLATE TEST ENDPOINTS (12 TEMPLATES) ====================

    /** 1. Test Party Activation Template */
    @PostMapping("/template/party-activation")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testPartyActivation(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "ABC Agro Foods") String partyName,
            @RequestParam(defaultValue = "https://tradex.com/activate?code=PRT-001") String link,
            @RequestParam(defaultValue = "TradeX Commodities") String businessName) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PartyActivationTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PartyActivationTemplate.builder()
                        .partyName(partyName)
                        .secureLink(link)
                        .businessName(businessName)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendPartyActivation(targetPhone, template),
                "Party Activation template sent"
        ));
    }

    /** 2. Test New Market Listing Template */
    @PostMapping("/template/new-listing")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testNewListing(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Wheat Grade-A") String commodity,
            @RequestParam(defaultValue = "500") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2450") String rate,
            @RequestParam(defaultValue = "Indore Mandi") String location,
            @RequestParam(defaultValue = "2026-10-15") String availableFrom,
            @RequestParam(defaultValue = "https://tradex.com/listings/101") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NewListingTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NewListingTemplate.builder()
                        .commodity(commodity).quantity(quantity).unit(unit).rate(rate)
                        .location(location).availableFrom(availableFrom).listingLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendNewListing(targetPhone, template),
                "New Listing template sent"
        ));
    }

    /** 3. Test Rate Updated Template */
    @PostMapping("/template/rate-updated")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testRateUpdated(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Soybean") String commodity,
            @RequestParam(defaultValue = "4800") String currentRate,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "350") String availableQuantity,
            @RequestParam(defaultValue = "Nagpur") String location,
            @RequestParam(defaultValue = "https://tradex.com/market/soybean") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.RateUpdatedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.RateUpdatedTemplate.builder()
                        .commodity(commodity).currentRate(currentRate).unit(unit)
                        .availableQuantity(availableQuantity).location(location).marketLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendRateUpdated(targetPhone, template),
                "Rate Updated template sent"
        ));
    }

    /** 4. Test Bid Submitted Template (Buyer) */
    @PostMapping("/template/bid-submitted")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testBidSubmitted(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Maize") String commodity,
            @RequestParam(defaultValue = "200") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2150") String rate,
            @RequestParam(defaultValue = "BID-2026-1001") String bidId,
            @RequestParam(defaultValue = "https://tradex.com/bids/1001") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidSubmittedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidSubmittedTemplate.builder()
                        .commodity(commodity).bidQuantity(quantity).unit(unit).bidRate(rate)
                        .bidId(bidId).bidLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendBidSubmitted(targetPhone, template),
                "Bid Submitted template sent"
        ));
    }

    /** 5. Test Bid Received Template (Seller) */
    @PostMapping("/template/bid-received")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testBidReceived(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Maize") String commodity,
            @RequestParam(defaultValue = "200") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2150") String rate,
            @RequestParam(defaultValue = "BID-2026-1001") String bidId,
            @RequestParam(defaultValue = "https://tradex.com/bids/review/1001") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidReceivedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidReceivedTemplate.builder()
                        .commodity(commodity).bidQuantity(quantity).unit(unit).bidRate(rate)
                        .bidId(bidId).reviewLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendBidReceived(targetPhone, template),
                "Bid Received template sent"
        ));
    }

    /** 6. Test Counter Offer Template */
    @PostMapping("/template/counter-offer")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testCounterOffer(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Wheat") String commodity,
            @RequestParam(defaultValue = "300") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2400") String counterRate,
            @RequestParam(defaultValue = "https://tradex.com/negotiate/55") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.CounterOfferTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.CounterOfferTemplate.builder()
                        .commodity(commodity).quantity(quantity).unit(unit)
                        .counterRate(counterRate).negotiationLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendCounterOffer(targetPhone, template),
                "Counter Offer template sent"
        ));
    }

    /** 7. Test Deal Agreed Template */
    @PostMapping("/template/deal-agreed")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDealAgreed(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Mustard Seed") String commodity,
            @RequestParam(defaultValue = "150") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "5200") String finalRate,
            @RequestParam(defaultValue = "DEAL-789") String dealId,
            @RequestParam(defaultValue = "https://tradex.com/deals/789") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealAgreedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealAgreedTemplate.builder()
                        .commodity(commodity).quantity(quantity).unit(unit)
                        .finalRate(finalRate).dealId(dealId).dealLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDealAgreed(targetPhone, template),
                "Deal Agreed template sent"
        ));
    }

    /** 8. Test Deal Confirmed Template */
    @PostMapping("/template/deal-confirmed")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDealConfirmed(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Mustard Seed") String commodity,
            @RequestParam(defaultValue = "150") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "5200") String finalRate,
            @RequestParam(defaultValue = "DEAL-789") String dealId,
            @RequestParam(defaultValue = "https://tradex.com/deals/789") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealConfirmedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealConfirmedTemplate.builder()
                        .commodity(commodity).quantity(quantity).unit(unit)
                        .finalRate(finalRate).dealId(dealId).dealLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDealConfirmed(targetPhone, template),
                "Deal Confirmed template sent"
        ));
    }

    /** 9. Test Quantity Locked Template */
    @PostMapping("/template/quantity-locked")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testQuantityLocked(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "DEAL-789") String dealId,
            @RequestParam(defaultValue = "Chana / Gram") String commodity,
            @RequestParam(defaultValue = "100") String lockedQuantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "400") String remainingQuantity,
            @RequestParam(defaultValue = "https://tradex.com/deals/789/status") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.QuantityLockedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.QuantityLockedTemplate.builder()
                        .dealId(dealId).commodity(commodity).lockedQuantity(lockedQuantity)
                        .unit(unit).remainingQuantity(remainingQuantity).transactionLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendQuantityLocked(targetPhone, template),
                "Quantity Locked template sent"
        ));
    }

    /** 10. Test Contract Ready Template */
    @PostMapping("/template/contract-ready")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testContractReady(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "TRX-2026-789-1001") String contractNumber,
            @RequestParam(defaultValue = "Soybean") String commodity,
            @RequestParam(defaultValue = "100") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "4850") String finalRate,
            @RequestParam(defaultValue = "https://tradex.com/contracts/501") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractReadyTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractReadyTemplate.builder()
                        .contractNumber(contractNumber).commodity(commodity).quantity(quantity)
                        .unit(unit).finalRate(finalRate).contractLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendContractReady(targetPhone, template),
                "Contract Ready template sent"
        ));
    }

    /** 11. Test Contract Approved Template */
    @PostMapping("/template/contract-approved")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testContractApproved(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "TRX-2026-789-1001") String contractNumber,
            @RequestParam(defaultValue = "Soybean") String commodity,
            @RequestParam(defaultValue = "100") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "4850") String finalRate,
            @RequestParam(defaultValue = "https://tradex.com/contracts/501") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractApprovedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractApprovedTemplate.builder()
                        .contractNumber(contractNumber).commodity(commodity).quantity(quantity)
                        .unit(unit).finalRate(finalRate).contractLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendContractApproved(targetPhone, template),
                "Contract Approved template sent"
        ));
    }

    /** 12. Test Contract Locked Template */
    @PostMapping("/template/contract-locked")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testContractLocked(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "TRX-2026-789-1001") String contractNumber,
            @RequestParam(defaultValue = "Soybean") String commodity,
            @RequestParam(defaultValue = "100") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "4850") String finalRate,
            @RequestParam(defaultValue = "https://tradex.com/contracts/501") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractLockedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.ContractLockedTemplate.builder()
                        .contractNumber(contractNumber).commodity(commodity).quantity(quantity)
                        .unit(unit).finalRate(finalRate).contractLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendContractLocked(targetPhone, template),
                "Contract Locked template sent"
        ));
    }

    /** 13. Test Demand Created Template */
    @PostMapping("/template/demand-created")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDemandCreated(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "DEM-0091") String demandNumber,
            @RequestParam(defaultValue = "Yellow Maize") String commodity,
            @RequestParam(defaultValue = "150.00") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2250.00") String targetRate,
            @RequestParam(defaultValue = "Indore Processing Hub") String destination,
            @RequestParam(defaultValue = "2026-10-25") String validTill,
            @RequestParam(defaultValue = "https://tradex.com/demands/DEM-0091") String link) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandCreatedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandCreatedTemplate.builder()
                        .demandNumber(demandNumber)
                        .commodity(commodity)
                        .quantity(quantity)
                        .unit(unit)
                        .targetRate(targetRate)
                        .destination(destination)
                        .validTill(validTill)
                        .demandLink(link)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDemandCreated(targetPhone, template),
                "Demand Created template sent"
        ));
    }

    /** 14. Test Demand Opportunity Alert Template */
    @PostMapping("/template/demand-alert")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDemandAlert(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "Yellow Maize") String commodity,
            @RequestParam(defaultValue = "150.00") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "2250.00") String targetRate,
            @RequestParam(defaultValue = "Central Zone") String zone,
            @RequestParam(defaultValue = "DEM-0091") String demandId,
            @RequestParam(defaultValue = "https://tradex.com/market/demands/DEM-0091") String quoteLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandAlertTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DemandAlertTemplate.builder()
                        .commodity(commodity)
                        .requiredQuantity(quantity)
                        .unit(unit)
                        .targetRate(targetRate)
                        .destinationZone(zone)
                        .demandId(demandId)
                        .quoteLink(quoteLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDemandAlert(targetPhone, template),
                "Demand Alert template sent"
        ));
    }

    /** 15. Test Bid Accepted Template */
    @PostMapping("/template/bid-accepted")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testBidAccepted(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "BID-0182") String bidNumber,
            @RequestParam(defaultValue = "Soybean Grade A") String commodity,
            @RequestParam(defaultValue = "80.00") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "4850.00") String agreedRate,
            @RequestParam(defaultValue = "DEA-0044") String dealId,
            @RequestParam(defaultValue = "https://tradex.com/deals/DEA-0044") String dealLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidAcceptedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidAcceptedTemplate.builder()
                        .bidNumber(bidNumber)
                        .commodity(commodity)
                        .quantity(quantity)
                        .unit(unit)
                        .agreedRate(agreedRate)
                        .dealId(dealId)
                        .dealLink(dealLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendBidAccepted(targetPhone, template),
                "Bid Accepted template sent"
        ));
    }

    /** 16. Test Bid Rejected Template */
    @PostMapping("/template/bid-rejected")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testBidRejected(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "BID-0182") String bidNumber,
            @RequestParam(defaultValue = "Soybean Grade A") String commodity,
            @RequestParam(defaultValue = "80.00") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "4600.00") String bidRate,
            @RequestParam(defaultValue = "Seller accepted higher counter offer") String reason,
            @RequestParam(defaultValue = "https://tradex.com/market") String marketLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidRejectedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.BidRejectedTemplate.builder()
                        .bidNumber(bidNumber)
                        .commodity(commodity)
                        .quantity(quantity)
                        .unit(unit)
                        .bidRate(bidRate)
                        .reason(reason)
                        .marketplaceLink(marketLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendBidRejected(targetPhone, template),
                "Bid Rejected template sent"
        ));
    }

    /** 17. Test Negotiation Rejected Template */
    @PostMapping("/template/negotiation-rejected")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testNegotiationRejected(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "NEG-0012") String negotiationCode,
            @RequestParam(defaultValue = "Wheat Sharbati") String commodity,
            @RequestParam(defaultValue = "3100.00") String lastOffer,
            @RequestParam(defaultValue = "Counterparty exceeded max budget limit") String reason,
            @RequestParam(defaultValue = "https://tradex.com/market") String marketLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NegotiationRejectedTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.NegotiationRejectedTemplate.builder()
                        .negotiationCode(negotiationCode)
                        .commodity(commodity)
                        .lastOffer(lastOffer)
                        .reason(reason)
                        .marketplaceLink(marketLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendNegotiationRejected(targetPhone, template),
                "Negotiation Rejected template sent"
        ));
    }

    /** 18. Test Deal Cancelled Template */
    @PostMapping("/template/deal-cancelled")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDealCancelled(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "DEA-0044") String dealNumber,
            @RequestParam(defaultValue = "Yellow Maize") String commodity,
            @RequestParam(defaultValue = "100.00") String quantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "Signing deadline lapsed without party execution") String reason,
            @RequestParam(defaultValue = "https://tradex.com/support") String supportLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealCancelledTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DealCancelledTemplate.builder()
                        .dealNumber(dealNumber)
                        .commodity(commodity)
                        .quantity(quantity)
                        .unit(unit)
                        .reason(reason)
                        .supportLink(supportLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDealCancelled(targetPhone, template),
                "Deal Cancelled template sent"
        ));
    }

    /** 19. Test Dispatch Update Template */
    @PostMapping("/template/dispatch-update")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testDispatchUpdate(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "CTR-0021") String contractNumber,
            @RequestParam(defaultValue = "Yellow Maize") String commodity,
            @RequestParam(defaultValue = "40.00") String dispatchQuantity,
            @RequestParam(defaultValue = "MT") String unit,
            @RequestParam(defaultValue = "MP-09-AB-1234") String vehicleNumber,
            @RequestParam(defaultValue = "+919876500000") String driverContact,
            @RequestParam(defaultValue = "2026-10-07") String dispatchDate,
            @RequestParam(defaultValue = "https://tradex.com/tracking/CTR-0021") String trackingLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DispatchUpdateTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.DispatchUpdateTemplate.builder()
                        .contractNumber(contractNumber)
                        .commodity(commodity)
                        .dispatchQuantity(dispatchQuantity)
                        .unit(unit)
                        .vehicleNumber(vehicleNumber)
                        .driverContact(driverContact)
                        .dispatchDate(dispatchDate)
                        .trackingLink(trackingLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendDispatchUpdate(targetPhone, template),
                "Dispatch Update template sent"
        ));
    }

    /** 20. Test Payment Settlement Update Template */
    @PostMapping("/template/payment-update")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testPaymentUpdate(
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "CTR-0021") String contractNumber,
            @RequestParam(defaultValue = "Escrow 20% Advance") String paymentStage,
            @RequestParam(defaultValue = "1,80,000.00") String amount,
            @RequestParam(defaultValue = "UTR-HDFC0099881122") String transactionRef,
            @RequestParam(defaultValue = "SETTLED_IN_ESCROW") String status,
            @RequestParam(defaultValue = "https://tradex.com/receipts/CTR-0021") String receiptLink) {
        String targetPhone = (phone != null && !phone.isEmpty()) ? phone : whatsAppConfig.getFromNumber();
        com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PaymentUpdateTemplate template =
                com.kalibyte.tradex.notification.dto.template.TradexTemplateDtos.PaymentUpdateTemplate.builder()
                        .contractNumber(contractNumber)
                        .paymentStage(paymentStage)
                        .amount(amount)
                        .transactionRef(transactionRef)
                        .status(status)
                        .receiptLink(receiptLink)
                        .build();
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendPaymentUpdate(targetPhone, template),
                "Payment Settlement Update template sent"
        ));
    }

    /** Generic Template Test */
    @PostMapping("/template/send-custom")
    public ResponseEntity<ApiResponse<WhatsAppMessageResponse>> testCustomTemplate(
            @RequestParam String phone,
            @RequestParam String templateName,
            @RequestParam(defaultValue = "en_US") String lang,
            @RequestBody(required = false) java.util.List<String> params) {
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.sendTemplateMessage(phone, templateName, lang, params, null, "CUSTOM_TEMPLATE"),
                "Custom template dispatched"
        ));
    }

    /**
     * Mask phone number for logging
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * Configuration Check Response DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ConfigCheckResponse {
        private String fromNumber;
        private boolean enabled;
        private boolean apiKeyConfigured;
        private boolean baseUrlConfigured;
        private int timeoutSeconds;
        private int retryAttempts;
        private long dispatchDelayMs;
        private String message;
    }
}
