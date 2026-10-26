package com.kalibyte.tradex.common.config;

import com.kalibyte.tradex.party.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    private final RoleService roleService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing default roles...");
        try {
            roleService.initializeDefaultRoles();
            log.info("Default roles initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default roles", e);
        }
    }
}
