package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.service.FileUploadService;
import com.gemini.deepresearch.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller for file upload-related endpoints.
 */
@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private PromptService promptService;
    
    /**
     * Upload a text file containing research prompts.
     * Each line in the file is considered a separate prompt.
     * 
     * @param file The uploaded file
     * @param notificationEmail Email for notifications (optional)
     * @param notificationPhone Phone for notifications (optional)
     * @return Upload result with the number of prompts created
     */
    @PostMapping("/text")
    public ResponseEntity<?> uploadTextFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notificationEmail", required = false) String notificationEmail,
            @RequestParam(value = "notificationPhone", required = false) String notificationPhone) {
        
        log.info("Text file upload received: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Uploaded file is empty"));
        }
        
        if (!fileUploadService.isValidTextFile(file)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Only text files are supported"));
        }
        
        try {
            // Extract prompts from the text file
            List<String> prompts = fileUploadService.extractPromptsFromTextFile(file);
            
            if (prompts.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No valid prompts found in the uploaded file"));
            }
            
            // Create batch prompts
            int count = promptService.createBatchPrompts(prompts, "FILE", notificationEmail, notificationPhone);
            
            return ResponseEntity.ok(Map.of(
                    "message", "File processed successfully",
                    "fileName", file.getOriginalFilename(),
                    "promptsCreated", count
            ));
            
        } catch (Exception e) {
            log.error("Error processing uploaded file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing file: " + e.getMessage()));
        }
    }
}