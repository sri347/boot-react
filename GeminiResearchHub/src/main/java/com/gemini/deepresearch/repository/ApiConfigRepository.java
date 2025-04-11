package com.gemini.deepresearch.repository;

import com.gemini.deepresearch.model.ApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for API configuration entities.
 */
@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfig, Long> {
    
    /**
     * Find configuration by type.
     * 
     * @param configType The configuration type
     * @return The configuration, if found
     */
    Optional<ApiConfig> findByConfigType(ApiConfig.ConfigType configType);
    
    /**
     * Find active configuration by type.
     * 
     * @param configType The configuration type
     * @param isActive The active status
     * @return The configuration, if found
     */
    Optional<ApiConfig> findByConfigTypeAndIsActive(ApiConfig.ConfigType configType, Boolean isActive);
}