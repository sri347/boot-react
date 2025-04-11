package com.gemini.deepresearch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for prompt creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {
    
    @NotBlank(message = "Prompt content is required")
    @Size(min = 3, max = 2000, message = "Prompt content must be between 3 and 2000 characters")
    private String content;
    
    private String createdBy;
    
    private String source;
    
    private String notificationEmail;
    
    private String notificationPhone;
    
    private boolean sendSms;
    
    private boolean sendWhatsapp;
}