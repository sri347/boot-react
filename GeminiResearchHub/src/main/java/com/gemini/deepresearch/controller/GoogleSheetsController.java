package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.service.ApiConfigService;
import com.gemini.deepresearch.service.GoogleSheetsService;
import com.gemini.deepresearch.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for Google Sheets integration endpoints.
 */
@RestController
@RequestMapping("/api/sheets")
@Slf4j
public class GoogleSheetsController {

    @Autowired
    private GoogleSheetsService googleSheetsService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    @Autowired
    private PromptService promptService;
    
    /**
     * Process prompts from a Google Sheet.
     * 
     * @param spreadsheetId The ID of the spreadsheet
     * @param range The range of cells to read (e.g., "Sheet1!A2:A10")
     * @param notificationEmail Email for notifications (optional)
     * @param notificationPhone Phone for notifications (optional)
     * @return Processing result with the number of prompts created
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSheetPrompts(
            @RequestParam("spreadsheetId") String spreadsheetId,
            @RequestParam("range") String range,
            @RequestParam(value = "notificationEmail", required = false) String notificationEmail,
            @RequestParam(value = "notificationPhone", required = false) String notificationPhone) {
        
        log.info("Google Sheets process request received for spreadsheet: {}, range: {}", spreadsheetId, range);
        
        // Check if Google Sheets integration is available
        if (!apiConfigService.isGoogleSheetsAvailable()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Google Sheets integration is not available. Please check your API credentials."));
        }
        
        try {
            // Read prompts from the sheet
            List<String> prompts = googleSheetsService.readPromptsFromSheet(spreadsheetId, range);
            
            if (prompts.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No valid prompts found in the specified sheet range"));
            }
            
            // Create batch prompts
            int count = promptService.createBatchPrompts(prompts, "SHEETS", notificationEmail, notificationPhone);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Google Sheet processed successfully",
                    "spreadsheetId", spreadsheetId,
                    "range", range,
                    "promptsCreated", count
            ));
            
        } catch (Exception e) {
            log.error("Error processing Google Sheet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing Google Sheet: " + e.getMessage()));
        }
    }
}