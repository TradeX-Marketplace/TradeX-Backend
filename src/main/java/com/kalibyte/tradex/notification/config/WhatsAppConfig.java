package com.kalibyte.tradex.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WhatsApp Configuration
 * 
 * Loads BookBey WhatsApp API configuration from application.yaml
 * 
 * Properties:
 * - api-key: BookBey WhatsApp API key
 * - base-url: BookBey API base URL
 * - from-number: Sender WhatsApp number (your business number from BookBey)
 * - from-name: Sender name to display in messages
 * - enabled: Enable/disable WhatsApp messaging
 * - timeout-seconds: API call timeout
 * - retry-attempts: Number of retry attempts
 * - retry-delay-ms: Delay between retries in milliseconds
 */
@Component
@ConfigurationProperties(prefix = "spring.whatsapp")
@Data
public class WhatsAppConfig {
    
    private String apiKey;
    private String baseUrl;
    private String fromNumber;
    private String fromName;
    private String adminPhone = "9321544177"; // Admin WhatsApp alert recipient
    private boolean enabled = true;
    private int timeoutSeconds = 30;
    private int retryAttempts = 3;
    private long retryDelayMs = 1000;
    private long dispatchDelayMs = 800; // Delay between message dispatches (up to 0.8s)
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.isEmpty() 
            && baseUrl != null && !baseUrl.isEmpty()
            && fromNumber != null && !fromNumber.isEmpty()
            && enabled;
    }
    
    /**
     * Get complete API endpoint for sending messages
     */
    public String getMessageEndpoint() {
        if (baseUrl == null) return "https://www.bookbey.com/public/api/send.php";
        return baseUrl.endsWith("/send.php") ? baseUrl : (baseUrl.replaceAll("/+$", "") + "/send.php");
    }
    
    /**
     * Get complete API endpoint for checking message status / quota
     */
    public String getStatusEndpoint() {
        if (baseUrl == null) return "https://www.bookbey.com/public/api/status.php";
        return baseUrl.endsWith("/status.php") ? baseUrl : (baseUrl.replaceAll("/+$", "") + "/status.php");
    }
}
