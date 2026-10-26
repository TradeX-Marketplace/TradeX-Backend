package com.kalibyte.tradex.notification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WhatsApp Message Response DTO
 * 
 * Response from BookBey WhatsApp API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessageResponse {
    
    /**
     * Success flag (mapped from BookBey 'ok')
     */
    @JsonProperty("ok")
    @JsonAlias({"success", "ok"})
    private boolean success;
    
    /**
     * Response message
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Message ID from BookBey API
     */
    @JsonProperty("id")
    @JsonAlias({"messageId", "id", "msg_id"})
    private String messageId;
    
    /**
     * Recipient phone number
     */
    private String phone;
    
    /**
     * Message status: sent, delivered, read, failed, etc.
     */
    private String status;
    
    /**
     * Remaining quota
     */
    private Integer remaining;
    
    /**
     * Error code if failed (mapped from BookBey 'error')
     */
    @JsonProperty("error")
    @JsonAlias({"errorCode", "error"})
    private String errorCode;
    
    /**
     * Error message if failed
     */
    private String errorMessage;
    
    /**
     * Timestamp of API response
     */
    private LocalDateTime timestamp;
    
    /**
     * Reference ID from request
     */
    private String referenceId;
    
    /**
     * Reference type from request
     */
    private String referenceType;
}
