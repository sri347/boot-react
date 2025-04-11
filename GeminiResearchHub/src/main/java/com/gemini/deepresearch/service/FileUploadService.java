package com.gemini.deepresearch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling file uploads.
 */
@Service
@Slf4j
public class FileUploadService {

    /**
     * Check if the file is a valid text file.
     * 
     * @param file The file to check
     * @return True if the file is a valid text file, false otherwise
     */
    public boolean isValidTextFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Valid content types for text files
        return contentType.equals("text/plain") ||
               contentType.equals("text/csv") ||
               contentType.equals("text/markdown") ||
               contentType.equals("application/octet-stream");
    }
    
    /**
     * Extract prompts from a text file.
     * Each line in the file is considered a separate prompt.
     * 
     * @param file The text file
     * @return List of prompts extracted from the file
     */
    public List<String> extractPromptsFromTextFile(MultipartFile file) {
        List<String> prompts = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            prompts = reader.lines()
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());
            
            log.info("Extracted {} prompts from file: {}", prompts.size(), file.getOriginalFilename());
            
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
        
        return prompts;
    }
}