package com.kalibyte.tradex.notification.exception;

/**
 * WhatsApp Exception
 * 
 * Custom exception for WhatsApp messaging failures
 */
public class WhatsAppException extends RuntimeException {
    
    private String errorCode;
    
    public WhatsAppException(String message) {
        super(message);
    }
    
    public WhatsAppException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WhatsAppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public WhatsAppException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
