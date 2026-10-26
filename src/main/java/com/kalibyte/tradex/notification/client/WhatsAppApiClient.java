package com.kalibyte.tradex.notification.client;

import com.kalibyte.tradex.notification.config.WhatsAppConfig;
import com.kalibyte.tradex.notification.dto.WhatsAppMessageRequest;
import com.kalibyte.tradex.notification.dto.WhatsAppMessageResponse;
import com.kalibyte.tradex.notification.exception.WhatsAppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

/**
 * WhatsApp API Client
 * 
 * Handles communication with BookBey WhatsApp API
 * Implements retry logic and error handling
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppApiClient {
    
    private final WhatsAppConfig whatsAppConfig;
    private final RestTemplate restTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final java.util.concurrent.atomic.AtomicLong lastDispatchTimestamp = new java.util.concurrent.atomic.AtomicLong(0);
    
    /**
     * Send WhatsApp message via BookBey API
     * 
     * @param request WhatsApp message request
     * @return WhatsAppMessageResponse
     * @throws WhatsAppException if sending fails after retries
     */
    public WhatsAppMessageResponse sendMessage(WhatsAppMessageRequest request) 
            throws WhatsAppException {
        
        if (!whatsAppConfig.isValid()) {
            log.error("WhatsApp configuration is invalid or disabled");
            throw new WhatsAppException("WhatsApp service is not configured");
        }
        
        // Enforce delay of up to 0.8 seconds (800ms) between outgoing messages
        applyDispatchDelay();
        
        return sendWithRetry(request, 0);
    }
    
    /**
     * Enforces a pacing delay of up to 0.8 seconds (800ms) between outgoing message requests
     * across all threads to prevent rate limit penalties and ensure smooth delivery.
     */
    private void applyDispatchDelay() {
        long delayConfig = whatsAppConfig.getDispatchDelayMs();
        if (delayConfig <= 0) {
            return;
        }

        // Cap maximum interval at 800ms (0.8s)
        long targetInterval = Math.min(delayConfig, 800);

        while (true) {
            long now = System.currentTimeMillis();
            long last = lastDispatchTimestamp.get();
            long nextAllowed = (last == 0) ? now : Math.max(now, last + targetInterval);

            if (lastDispatchTimestamp.compareAndSet(last, nextAllowed)) {
                long waitTime = nextAllowed - now;
                if (waitTime > 0) {
                    try {
                        log.debug("Applying WhatsApp message sending delay of {} ms (up to 0.8s)", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("WhatsApp dispatch throttle interrupted");
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Send message with retry logic
     */
    private WhatsAppMessageResponse sendWithRetry(WhatsAppMessageRequest request, int attempt) 
            throws WhatsAppException {
        
        try {
            log.info("Sending WhatsApp message to: {} (Attempt {}/{})", 
                     request.getPhone(), 
                     attempt + 1, 
                     whatsAppConfig.getRetryAttempts());
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + whatsAppConfig.getApiKey());
            headers.set("X-API-Key", whatsAppConfig.getApiKey());
            
            // BookBey Web Engine check: Web Engine connected via QR does not support Meta Cloud API templates.
            // If messageType is 'template' but formatted text is provided, default to 'text' for instant delivery.
            boolean isTemplate = "template".equalsIgnoreCase(request.getMessageType());
            String resolvedType = isTemplate ? "template" : (request.getMessageType() != null ? request.getMessageType().toLowerCase() : "text");
            String resolvedText = request.getMessage();

            // When channel uses Web Engine, sending template fails with HTTP 422 template_not_supported.
            // If pre-formatted text exists, directly send as text.
            if (isTemplate && resolvedText != null && !resolvedText.trim().isEmpty()) {
                resolvedType = "text";
            }

            BookBeyMessagePayload payload = BookBeyMessagePayload.builder()
                    .to(normalizePhoneNumber(request.getPhone()))
                    .type(resolvedType)
                    .text(resolvedText)
                    .template("template".equals(resolvedType) ? request.getTemplate() : null)
                    .lang("template".equals(resolvedType) ? (request.getLang() != null ? request.getLang() : "en_US") : null)
                    .params("template".equals(resolvedType) ? request.getParams() : null)
                    .mediaUrl(request.getMediaUrl())
                    .caption(request.getCaption())
                    .build();
            
            String jsonBody = objectMapper.writeValueAsString(payload);
            headers.setContentLength(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);
            
            // BookBey API supports POST and PUT; POST guarantees proper JSON body streaming without chunked issues
            org.springframework.http.ResponseEntity<WhatsAppMessageResponse> responseEntity = restTemplate.exchange(
                    whatsAppConfig.getMessageEndpoint(),
                    org.springframework.http.HttpMethod.POST,
                    httpEntity,
                    WhatsAppMessageResponse.class
            );
            
            WhatsAppMessageResponse response = responseEntity.getBody();
            
            if (response == null) {
                throw new WhatsAppException("Null response from WhatsApp API");
            }
            
            if (response.isSuccess()) {
                log.info("Message sent successfully. MessageId: {}, Phone: {}", 
                         response.getMessageId(), 
                         request.getPhone());
                
                // Preserve original reference information
                response.setPhone(request.getPhone());
                response.setReferenceId(request.getReferenceId());
                response.setReferenceType(request.getReferenceType());
                response.setTimestamp(java.time.LocalDateTime.now());
                
                return response;
            } else {
                String error = response.getErrorCode() != null ? response.getErrorCode() : "UNKNOWN_ERROR";
                String msg = response.getMessage() != null ? response.getMessage() : response.getErrorMessage();
                throw new WhatsAppException("API returned error: " + msg + " (Code: " + error + ")");
            }
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            log.warn("BookBey API HTTP error {}: {}", e.getStatusCode(), errorBody);

            // Handle Web engine fallback when BookBey returns 422 template_not_supported
            if (errorBody.contains("template_not_supported") 
                    && request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
                log.info("BookBey channel uses Web engine (template_not_supported). Automatically falling back to formatted text for {}", request.getPhone());
                request.setMessageType("text");
                request.setTemplate(null);
                request.setParams(null);
                return sendWithRetry(request, attempt);
            }

            if (attempt < whatsAppConfig.getRetryAttempts() - 1) {
                try {
                    long delayMs = whatsAppConfig.getRetryDelayMs() * (attempt + 1);
                    log.info("Retrying in {} ms", delayMs);
                    Thread.sleep(delayMs);
                    return sendWithRetry(request, attempt + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new WhatsAppException("Retry interrupted", ie);
                }
            }

            throw new WhatsAppException("BookBey API returned error: " + errorBody, e);
        } catch (Exception e) {
            log.warn("API call failed: {}", e.getMessage());
            
            // Retry logic
            if (attempt < whatsAppConfig.getRetryAttempts() - 1) {
                try {
                    long delayMs = whatsAppConfig.getRetryDelayMs() * (attempt + 1);
                    log.info("Retrying in {} ms", delayMs);
                    Thread.sleep(delayMs);
                    return sendWithRetry(request, attempt + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new WhatsAppException("Retry interrupted", ie);
                }
            }
            
            throw new WhatsAppException(
                "Failed to send WhatsApp message after " + whatsAppConfig.getRetryAttempts() + " attempts", e
            );
        }
    }
    
    /**
     * Normalize phone number according to BookBey requirement:
     * Country code + number, digits only, no + sign (e.g. 9198XXXXXXXX)
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new WhatsAppException("Phone number is required");
        }
        
        // Strip everything that is not a digit
        String digits = phone.replaceAll("[^0-9]", "");
        
        // If 10 digits (standard Indian mobile), prepend 91
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        
        return digits;
    }
    
    /**
     * BookBey API Payload
     * JSON body format: {"to":"9198XXXXXXXX","type":"text","text":"Hello!"}
     */
    @lombok.Data
    @lombok.Builder
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class BookBeyMessagePayload {
        private String to;            // Recipient WhatsApp number (digits only with country code)
        private String type;          // text, image, document, template
        private String text;          // Message text content
        private String template;      // Template name (e.g. tradex_party_activation)
        private String lang;          // Language code (e.g. en_US)
        private java.util.List<String> params; // Template variables ordered
        private String mediaUrl;      // Optional media URL
        private String caption;       // Optional caption
    }
}
