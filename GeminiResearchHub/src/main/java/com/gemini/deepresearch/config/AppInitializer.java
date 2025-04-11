package com.gemini.deepresearch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

/**
 * Application initializer that runs on startup.
 * Checks for required API keys and performs initialization tasks.
 */
@Configuration
@Slf4j
public class AppInitializer {

    @Autowired
    private Environment environment;

    /**
     * CommandLineRunner bean that executes on application startup
     * to check for required API keys and perform initialization.
     * 
     * @return A CommandLineRunner to execute initialization tasks
     */
    @Bean
    public CommandLineRunner initializeApp() {
        return args -> {
            log.info("Application starting...");
            checkRequiredApiKeys();
            log.info("Application initialization complete");
        };
    }
    
    /**
     * Checks for required API keys and logs warnings if they are missing.
     * This ensures graceful degradation when certain services are unavailable.
     */
    private void checkRequiredApiKeys() {
        // Check Gemini API Key
        if (environment.getProperty("gemini.api.key") == null) {
            log.warn("GEMINI_API_KEY is not set. Gemini API features will not be available.");
        }
        
        // Check Twilio credentials
        if (environment.getProperty("twilio.account.sid") == null || 
            environment.getProperty("twilio.auth.token") == null ||
            environment.getProperty("twilio.phone.number") == null) {
            log.warn("One or more Twilio credentials are missing. SMS and WhatsApp notification features will not be available.");
        }
        
        // Check email credentials
        if (environment.getProperty("spring.mail.username") == null || 
            environment.getProperty("spring.mail.password") == null) {
            log.warn("Email credentials are missing. Email notification features will not be available.");
        }
        
        // Check Google Sheets credentials
        if (environment.getProperty("google.credentials") == null) {
            log.warn("Google credentials are missing. Google Sheets import features will not be available.");
        }
    }
}