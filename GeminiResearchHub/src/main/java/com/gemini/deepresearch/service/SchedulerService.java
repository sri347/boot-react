package com.gemini.deepresearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for scheduling background tasks.
 */
@Service
@Slf4j
public class SchedulerService {

    @Autowired
    private PromptService promptService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    /**
     * Scheduled task to process pending prompts.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processPendingPrompts() {
        log.info("Running scheduled task: process pending prompts");
        
        // Skip processing if the Gemini API is not available
        if (!apiConfigService.isGeminiApiAvailable()) {
            log.warn("Skipping scheduled prompt processing: Gemini API is not available");
            return;
        }
        
        int processed = promptService.processAllPendingPrompts();
        log.info("Scheduled task completed: processed {} pending prompts", processed);
    }
    
    /**
     * Scheduled task to check API status.
     * Runs every 15 minutes.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void checkApiStatus() {
        log.info("Running scheduled task: check API status");
        apiConfigService.refreshApiStatus();
        log.info("API status check completed");
    }
}