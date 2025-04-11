package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.ApiStatusResponse;
import com.gemini.deepresearch.service.ApiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for API status endpoints.
 */
@RestController
@RequestMapping("/api/status")
@Slf4j
public class ApiStatusController {

    @Autowired
    private ApiConfigService apiConfigService;
    
    /**
     * Get the status of all external APIs and services.
     * 
     * @return Status of all external APIs and services
     */
    @GetMapping
    public ResponseEntity<ApiStatusResponse> getApiStatus() {
        log.info("API status check requested");
        ApiStatusResponse status = apiConfigService.getApiStatus();
        return ResponseEntity.ok(status);
    }
}