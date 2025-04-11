package com.gemini.deepresearch.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Service for reading research prompts from Google Sheets.
 */
@Service
@Slf4j
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Gemini Deep Research";
    
    @Value("${google.credentials:}")
    private String googleCredentialsBase64;
    
    private Sheets sheetsService;
    private boolean initialized = false;
    
    /**
     * Initialize the Google Sheets service with credentials.
     * This method is called lazily when the service is first used.
     * 
     * @return true if initialization was successful, false otherwise
     */
    private boolean initialize() {
        if (initialized) {
            return true;
        }
        
        if (googleCredentialsBase64 == null || googleCredentialsBase64.isEmpty()) {
            log.warn("Google credentials are not set. Google Sheets features are disabled.");
            return false;
        }
        
        try {
            // Convert Base64 credentials to InputStream
            byte[] credentialsBytes = Base64.getDecoder().decode(googleCredentialsBase64);
            InputStream credentialsStream = new ByteArrayInputStream(credentialsBytes);
            
            // Load credentials
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets.readonly"));
            
            // Build the sheets service
            sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            
            initialized = true;
            return true;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to initialize Google Sheets service: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Read research prompts from a Google Sheet.
     * 
     * @param spreadsheetId The ID of the spreadsheet
     * @param range The range of cells to read (e.g., "Sheet1!A2:B10")
     * @return A list of research prompts, or an empty list if reading failed
     */
    public List<String> readPromptsFromSheet(String spreadsheetId, String range) {
        List<String> prompts = new ArrayList<>();
        
        if (!initialize()) {
            log.warn("Cannot read from Google Sheets: Service is not initialized");
            return prompts;
        }
        
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            
            List<List<Object>> values = response.getValues();
            
            if (values == null || values.isEmpty()) {
                log.warn("No data found in the specified range");
                return prompts;
            }
            
            // Extract prompts from the sheet
            // We assume the first column contains the prompts
            for (List<Object> row : values) {
                if (!row.isEmpty() && row.get(0) != null) {
                    String prompt = row.get(0).toString().trim();
                    if (!prompt.isEmpty()) {
                        prompts.add(prompt);
                    }
                }
            }
            
            log.info("Read {} prompts from Google Sheet", prompts.size());
            return prompts;
        } catch (IOException e) {
            log.error("Error reading from Google Sheets: {}", e.getMessage());
            return prompts;
        }
    }
    
    /**
     * Check if the Google Sheets service is available.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isApiAvailable() {
        return initialize();
    }
}