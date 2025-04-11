package com.gemini.deepresearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for API status endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStatusResponse {
    
    private boolean geminiApiAvailable;
    private boolean twilioSmsAvailable;
    private boolean twilioWhatsAppAvailable;
    private boolean emailServiceAvailable;
    private boolean googleSheetsApiAvailable;
}