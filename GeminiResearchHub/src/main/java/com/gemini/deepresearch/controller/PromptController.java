package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.PromptRequest;
import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.service.PromptService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for prompt-related endpoints.
 */
@RestController
@RequestMapping("/api/prompts")
@Slf4j
public class PromptController {

    @Autowired
    private PromptService promptService;
    
    /**
     * Create a new research prompt.
     * 
     * @param promptRequest The prompt creation request
     * @return The created prompt
     */
    @PostMapping
    public ResponseEntity<PromptResponse> createPrompt(@Valid @RequestBody PromptRequest promptRequest) {
        log.info("Create prompt request received: {}", promptRequest.getContent());
        PromptResponse response = promptService.createPrompt(promptRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get a prompt by its ID.
     * 
     * @param id The prompt ID
     * @return The prompt, if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptResponse> getPromptById(@PathVariable Long id) {
        log.info("Get prompt by ID request received: {}", id);
        return promptService.getPromptById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found with ID: " + id));
    }
    
    /**
     * Get all prompts.
     * 
     * @return A list of all prompts
     */
    @GetMapping
    public ResponseEntity<List<PromptResponse>> getAllPrompts() {
        log.info("Get all prompts request received");
        List<PromptResponse> prompts = promptService.getAllPrompts();
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * Get all prompts with a specific status.
     * 
     * @param status The status to filter by
     * @return A list of prompts with the given status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PromptResponse>> getPromptsByStatus(@PathVariable String status) {
        log.info("Get prompts by status request received: {}", status);
        List<PromptResponse> prompts = promptService.getPromptsByStatus(status);
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * Process a specific prompt.
     * 
     * @param id The ID of the prompt to process
     * @return The processed prompt
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<PromptResponse> processPrompt(@PathVariable Long id) {
        log.info("Process prompt request received for ID: {}", id);
        return promptService.processPrompt(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found with ID: " + id));
    }
}