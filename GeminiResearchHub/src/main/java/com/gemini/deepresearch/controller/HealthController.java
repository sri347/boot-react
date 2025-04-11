package com.gemini.deepresearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for health and monitoring endpoints.
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    /**
     * Basic health check endpoint.
     * 
     * @return Health status response
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}