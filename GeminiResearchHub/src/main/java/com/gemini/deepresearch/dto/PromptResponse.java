package com.gemini.deepresearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for prompt-related endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {
    
    private Long id;
    
    private String content;
    
    private String result;
    
    private String status;
    
    private String createdBy;
    
    private String source;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    private String notificationEmail;
    
    private String notificationPhone;
    
    private Boolean notificationSent;
}