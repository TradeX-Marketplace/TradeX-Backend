package com.kalibyte.tradex.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WhatsApp Message Request DTO
 * 
 * Payload to send messages via BookBey WhatsApp API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppMessageRequest {
    
    /**
     * Recipient phone number (with country code, e.g., +919876543210)
     */
    private String phone;
    
    /**
     * Message body/content
     */
    private String message;
    
    /**
     * Message type: TEXT, IMAGE, DOCUMENT, etc.
     */
    @Builder.Default
    private String messageType = "TEXT";
    
    /**
     * Optional media URL (for images, documents, etc.)
     */
    private String mediaUrl;
    
    /**
     * Optional caption (for media messages)
     */
    private String caption;

    /**
     * Template name for template messages (e.g. tradex_party_activation)
     */
    private String template;

    /**
     * Template language code (default "en_US")
     */
    @Builder.Default
    private String lang = "en_US";

    /**
     * Template positional parameters
     */
    private java.util.List<String> params;
    
    /**
     * Reference ID for tracking (bid ID, deal ID, etc.)
     */
    private String referenceId;
    
    /**
     * Reference type (BID, DEAL, SUPPLY, DEMAND, etc.)
     */
    private String referenceType;
}
