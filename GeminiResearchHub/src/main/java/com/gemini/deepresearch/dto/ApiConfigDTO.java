package com.gemini.deepresearch.dto;

import com.gemini.deepresearch.model.ApiConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for API configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiConfigDTO {
    
    private Long id;
    
    private ApiConfig.ConfigType configType;
    
    // Common fields
    private String apiKey;
    
    private String apiToken;
    
    private String username;
    
    private String password;
    
    // SMS/SignalWire specific fields
    private String projectId;
    
    private String spaceUrl;
    
    private String fromNumber;
    
    // Google specific fields
    private String clientId;
    
    private String clientSecret;
    
    private String refreshToken;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean isActive;
    
    /**
     * Convert entity to DTO.
     * 
     * @param config The entity
     * @return The DTO
     */
    public static ApiConfigDTO fromEntity(ApiConfig config) {
        return ApiConfigDTO.builder()
                .id(config.getId())
                .configType(config.getConfigType())
                .apiKey(config.getApiKey())
                .apiToken(config.getApiToken())
                .username(config.getUsername())
                .password(null) // Don't return password in responses
                .projectId(config.getProjectId())
                .spaceUrl(config.getSpaceUrl())
                .fromNumber(config.getFromNumber())
                .clientId(config.getClientId())
                .clientSecret(null) // Don't return client secret in responses
                .refreshToken(null) // Don't return refresh token in responses
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .isActive(config.getIsActive())
                .build();
    }
    
    /**
     * Convert DTO to entity.
     * 
     * @return The entity
     */
    public ApiConfig toEntity() {
        return ApiConfig.builder()
                .id(this.id)
                .configType(this.configType)
                .apiKey(this.apiKey)
                .apiToken(this.apiToken)
                .username(this.username)
                .password(this.password)
                .projectId(this.projectId)
                .spaceUrl(this.spaceUrl)
                .fromNumber(this.fromNumber)
                .clientId(this.clientId)
                .clientSecret(this.clientSecret)
                .refreshToken(this.refreshToken)
                .isActive(this.isActive != null ? this.isActive : true)
                .build();
    }
}