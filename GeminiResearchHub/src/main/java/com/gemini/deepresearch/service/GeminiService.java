package com.gemini.deepresearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Google's Gemini AI API.
 */
@Service
@Slf4j
public class GeminiService {
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    
    /**
     * Send a prompt to the Gemini API for deep research.
     * 
     * @param prompt The research prompt
     * @return The research result
     * @throws RuntimeException if the API is not available or the request fails
     */
    public String getResearchResult(String prompt) {
        if (!apiConfigService.isGeminiApiAvailable()) {
            log.error("Gemini API is not available. Please configure your API key in the admin settings.");
            throw new RuntimeException("Gemini API is not available. Please configure your API key in the admin settings.");
        }
        
        String apiKey = apiConfigService.getGeminiApiKey();
        
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            contents.put("parts", parts);
            contents.put("role", "user");
            
            List<Map<String, Object>> contentsList = new ArrayList<>();
            contentsList.add(contents);
            
            requestBody.put("contents", contentsList);
            requestBody.put("generationConfig", Map.of(
                    "temperature", 0.7,
                    "topP", 0.95,
                    "topK", 40,
                    "maxOutputTokens", 8192
            ));
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Send request to Gemini API
            String url = API_URL + "?key=" + apiKey;
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response == null) {
                throw new RuntimeException("Received null response from Gemini API");
            }
            
            // Parse response
            return extractTextFromResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Failed to get research result: " + e.getMessage());
        }
    }
    
    /**
     * Extract text from the Gemini API response.
     * 
     * @param response The API response
     * @return The extracted text
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates found in response");
            }
            
            Map<String, Object> candidate = candidates.get(0);
            List<Map<String, Object>> contents = (List<Map<String, Object>>) candidate.get("content");
            if (contents == null || contents.isEmpty()) {
                throw new RuntimeException("No content found in response");
            }
            
            Map<String, Object> content = contents.get(0);
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("No parts found in response");
            }
            
            Map<String, Object> part = parts.get(0);
            return (String) part.get("text");
            
        } catch (Exception e) {
            log.error("Error parsing Gemini API response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse research result: " + e.getMessage());
        }
    }
}