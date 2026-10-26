package com.kalibyte.tradex.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Configuration
 * 
 * Configures RestTemplate for making HTTP calls to WhatsApp API
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Configure RestTemplate with timeout settings
     */
    @Bean
    public RestTemplate restTemplate(WhatsAppConfig whatsAppConfig) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Create and configure the request factory with timeout settings
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) (whatsAppConfig.getTimeoutSeconds() * 1000));
        requestFactory.setReadTimeout((int) (whatsAppConfig.getTimeoutSeconds() * 1000));
        
        restTemplate.setRequestFactory(requestFactory);
        
        return restTemplate;
    }
}
