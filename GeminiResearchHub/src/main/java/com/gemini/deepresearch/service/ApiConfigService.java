package com.gemini.deepresearch.service;

import com.gemini.deepresearch.dto.ApiConfigDTO;
import com.gemini.deepresearch.dto.ApiStatusResponse;
import com.gemini.deepresearch.model.ApiConfig;
import com.gemini.deepresearch.repository.ApiConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing API configurations.
 */
@Service
@Slf4j
public class ApiConfigService {
    
    @Autowired
    private ApiConfigRepository apiConfigRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Check if the Gemini API is available.
     * 
     * @return True if the API is available, false otherwise
     */
    public boolean isGeminiApiAvailable() {
        Optional<ApiConfig> config = apiConfigRepository.findByConfigTypeAndIsActive(
                ApiConfig.ConfigType.GEMINI, true);
        
        boolean available = config.isPresent() && config.get().getApiKey() != null && 
                           !config.get().getApiKey().isBlank();
        
        log.debug("Gemini API available: {}", available);
        return available;
    }
    
    /**
     * Get the Gemini API key.
     * 
     * @return The API key, or empty string if not available
     */
    public String getGeminiApiKey() {
        return apiConfigRepository.findByConfigTypeAndIsActive(ApiConfig.ConfigType.GEMINI, true)
                .map(ApiConfig::getApiKey)
                .orElse("");
    }
    
    /**
     * Check if SMS is available.
     * 
     * @return True if SMS is available, false otherwise
     */
    public boolean isSmsAvailable() {
        return notificationService.isSmsConfigured();
    }
    
    /**
     * Check if WhatsApp is available.
     * Currently not supported with open source alternatives.
     * 
     * @return True if WhatsApp is available, false otherwise
     */
    public boolean isWhatsAppAvailable() {
        // WhatsApp is more challenging with open source alternatives
        return false;
    }
    
    /**
     * Check if email service is available.
     * 
     * @return True if the service is available, false otherwise
     */
    public boolean isEmailServiceAvailable() {
        return notificationService.isEmailConfigured();
    }
    
    /**
     * Check if Google Sheets API is available.
     * 
     * @return True if the API is available, false otherwise
     */
    public boolean isGoogleSheetsAvailable() {
        Optional<ApiConfig> config = apiConfigRepository.findByConfigTypeAndIsActive(
                ApiConfig.ConfigType.GOOGLE_SHEETS, true);
        
        boolean available = config.isPresent() && 
                (config.get().getRefreshToken() != null && !config.get().getRefreshToken().isBlank()) ||
                (config.get().getApiKey() != null && !config.get().getApiKey().isBlank());
        
        log.debug("Google Sheets API available: {}", available);
        return available;
    }
    
    /**
     * Get the status of all external APIs and services.
     * 
     * @return Status of all external APIs and services
     */
    public ApiStatusResponse getApiStatus() {
        return ApiStatusResponse.builder()
                .geminiApiAvailable(isGeminiApiAvailable())
                .twilioSmsAvailable(isSmsAvailable())
                .twilioWhatsAppAvailable(isWhatsAppAvailable())
                .emailServiceAvailable(isEmailServiceAvailable())
                .googleSheetsApiAvailable(isGoogleSheetsAvailable())
                .build();
    }
    
    /**
     * Get all API configurations.
     * 
     * @return List of all API configurations
     */
    public List<ApiConfigDTO> getAllConfigs() {
        return apiConfigRepository.findAll().stream()
                .map(ApiConfigDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get API configuration by ID.
     * 
     * @param id The configuration ID
     * @return The configuration, if found
     */
    public Optional<ApiConfigDTO> getConfigById(Long id) {
        return apiConfigRepository.findById(id)
                .map(ApiConfigDTO::fromEntity);
    }
    
    /**
     * Save API configuration.
     * 
     * @param configDTO The configuration to save
     * @return The saved configuration
     */
    public ApiConfigDTO saveConfig(ApiConfigDTO configDTO) {
        // If this is an active config, deactivate other configs of the same type
        if (Boolean.TRUE.equals(configDTO.getIsActive())) {
            apiConfigRepository.findByConfigType(configDTO.getConfigType())
                    .forEach(config -> {
                        if (!config.getId().equals(configDTO.getId())) {
                            config.setIsActive(false);
                            apiConfigRepository.save(config);
                        }
                    });
        }
        
        // If password/secret is empty and ID is not null, keep existing password/secret
        if (configDTO.getId() != null) {
            apiConfigRepository.findById(configDTO.getId())
                    .ifPresent(existingConfig -> {
                        if ((configDTO.getPassword() == null || configDTO.getPassword().isBlank()) && 
                            existingConfig.getPassword() != null) {
                            configDTO.setPassword(existingConfig.getPassword());
                        }
                        if ((configDTO.getApiKey() == null || configDTO.getApiKey().isBlank()) && 
                            existingConfig.getApiKey() != null) {
                            configDTO.setApiKey(existingConfig.getApiKey());
                        }
                        if ((configDTO.getApiToken() == null || configDTO.getApiToken().isBlank()) && 
                            existingConfig.getApiToken() != null) {
                            configDTO.setApiToken(existingConfig.getApiToken());
                        }
                        if ((configDTO.getClientSecret() == null || configDTO.getClientSecret().isBlank()) && 
                            existingConfig.getClientSecret() != null) {
                            configDTO.setClientSecret(existingConfig.getClientSecret());
                        }
                        if ((configDTO.getRefreshToken() == null || configDTO.getRefreshToken().isBlank()) && 
                            existingConfig.getRefreshToken() != null) {
                            configDTO.setRefreshToken(existingConfig.getRefreshToken());
                        }
                    });
        }
        
        ApiConfig savedConfig = apiConfigRepository.save(configDTO.toEntity());
        return ApiConfigDTO.fromEntity(savedConfig);
    }
    
    /**
     * Delete API configuration.
     * 
     * @param id The configuration ID
     */
    public void deleteConfig(Long id) {
        apiConfigRepository.deleteById(id);
    }
    
    /**
     * Test API configuration.
     * 
     * @param id The configuration ID
     * @return True if the test was successful, false otherwise
     */
    public boolean testConfig(Long id) {
        ApiConfig config = apiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        
        switch (config.getConfigType()) {
            case SMS:
                return testSmsConfig(config);
            case EMAIL:
                return testEmailConfig(config);
            case GEMINI:
                return testGeminiConfig(config);
            case GOOGLE_SHEETS:
                return testGoogleSheetsConfig(config);
            default:
                return false;
        }
    }
    
    /**
     * Test SMS configuration.
     * 
     * @param config The configuration to test
     * @return True if the test was successful, false otherwise
     */
    private boolean testSmsConfig(ApiConfig config) {
        if (config.getProjectId() == null || config.getProjectId().isBlank() ||
            config.getSpaceUrl() == null || config.getSpaceUrl().isBlank() ||
            config.getApiToken() == null || config.getApiToken().isBlank() ||
            config.getFromNumber() == null || config.getFromNumber().isBlank()) {
            
            return false;
        }
        
        try {
            // SignalWire API endpoint for getting account info (lightweight test)
            String url = "https://" + config.getSpaceUrl() + "/api/laml/2010-04-01/Accounts/" + 
                        config.getProjectId() + ".json";
            
            // Set up headers with Basic Authentication
            HttpHeaders headers = new HttpHeaders();
            String auth = config.getProjectId() + ":" + config.getApiToken();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            // Send request
            HttpEntity<String> entity = new HttpEntity<>(headers);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, entity);
            
            return response != null && response.containsKey("sid");
        } catch (Exception e) {
            log.error("Error testing SMS configuration: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Test email configuration.
     * 
     * @param config The configuration to test
     * @return True if the test was successful, false otherwise
     */
    private boolean testEmailConfig(ApiConfig config) {
        if (config.getUsername() == null || config.getUsername().isBlank() ||
            config.getPassword() == null || config.getPassword().isBlank()) {
            
            return false;
        }
        
        // Email testing would require setting up a JavaMailSender
        // For simplicity, we'll just validate the presence of credentials
        return true;
    }
    
    /**
     * Test Gemini API configuration.
     * 
     * @param config The configuration to test
     * @return True if the test was successful, false otherwise
     */
    private boolean testGeminiConfig(ApiConfig config) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            return false;
        }
        
        try {
            // Gemini API endpoint for models list (lightweight test)
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + config.getApiKey();
            
            // Send request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response != null && response.containsKey("models");
        } catch (Exception e) {
            log.error("Error testing Gemini API configuration: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Test Google Sheets API configuration.
     * 
     * @param config The configuration to test
     * @return True if the test was successful, false otherwise
     */
    private boolean testGoogleSheetsConfig(ApiConfig config) {
        // Google Sheets testing would require OAuth flow or API key validation
        // For simplicity, we'll just validate the presence of credentials
        return config.getApiKey() != null && !config.getApiKey().isBlank() ||
               config.getRefreshToken() != null && !config.getRefreshToken().isBlank();
    }
}