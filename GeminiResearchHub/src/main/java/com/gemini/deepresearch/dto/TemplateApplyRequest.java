package com.gemini.deepresearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for applying a template with variables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateApplyRequest {
    
    private Long templateId;
    
    private Map<String, String> variables;
    
    private String notificationEmail;
    
    private String notificationPhone;
    
    private Boolean sendSms;
    
    private Boolean sendWhatsapp;
}