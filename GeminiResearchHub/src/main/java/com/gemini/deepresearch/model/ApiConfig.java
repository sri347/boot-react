package com.gemini.deepresearch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing API configuration stored in the database.
 * This allows dynamic configuration without environment variables.
 */
@Entity
@Table(name = "api_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConfigType configType;
    
    // Common fields
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "api_token")
    private String apiToken;
    
    @Column
    private String username;
    
    @Column
    private String password;
    
    // SMS/SignalWire specific fields
    @Column(name = "project_id")
    private String projectId;
    
    @Column(name = "space_url")
    private String spaceUrl;
    
    @Column(name = "from_number")
    private String fromNumber;
    
    // Google specific fields
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "client_secret")
    private String clientSecret;
    
    @Column(name = "refresh_token")
    private String refreshToken;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    /**
     * Types of API configurations.
     */
    public enum ConfigType {
        SMS,
        EMAIL,
        GEMINI,
        GOOGLE_SHEETS
    }
}