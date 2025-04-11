package com.gemini.deepresearch.service;

import com.gemini.deepresearch.dto.PromptRequest;
import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.model.Prompt;
import com.gemini.deepresearch.repository.PromptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing research prompts.
 */
@Service
@Slf4j
public class PromptService {

    @Autowired
    private PromptRepository promptRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private TwilioService twilioService;
    
    /**
     * Create a new research prompt.
     * 
     * @param promptRequest The prompt creation request
     * @return The created prompt
     */
    @Transactional
    public PromptResponse createPrompt(PromptRequest promptRequest) {
        log.info("Creating new prompt: {}", promptRequest.getContent());
        
        Prompt prompt = Prompt.builder()
                .content(promptRequest.getContent())
                .status("PENDING")
                .source(promptRequest.getSource() != null ? promptRequest.getSource() : "WEB")
                .createdBy(promptRequest.getCreatedBy())
                .notificationEmail(promptRequest.isSendEmail() ? promptRequest.getNotificationEmail() : null)
                .notificationPhone(promptRequest.isSendSms() || promptRequest.isSendWhatsapp() ? promptRequest.getNotificationPhone() : null)
                .emailSent(false)
                .smsSent(false)
                .whatsappSent(false)
                .build();
        
        Prompt savedPrompt = promptRepository.save(prompt);
        log.info("Prompt created with ID: {}", savedPrompt.getId());
        
        return PromptResponse.fromEntity(savedPrompt);
    }
    
    /**
     * Get a prompt by its ID.
     * 
     * @param id The prompt ID
     * @return The prompt, if found
     */
    public Optional<PromptResponse> getPromptById(Long id) {
        return promptRepository.findById(id)
                .map(PromptResponse::fromEntity);
    }
    
    /**
     * Get all prompts.
     * 
     * @return A list of all prompts
     */
    public List<PromptResponse> getAllPrompts() {
        return promptRepository.findAll().stream()
                .map(PromptResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all prompts with a specific status.
     * 
     * @param status The status to filter by
     * @return A list of prompts with the given status
     */
    public List<PromptResponse> getPromptsByStatus(String status) {
        return promptRepository.findByStatus(status).stream()
                .map(PromptResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Process a specific prompt through the Gemini API.
     * 
     * @param id The ID of the prompt to process
     * @return The processed prompt, if found and successfully processed
     */
    @Transactional
    public Optional<PromptResponse> processPrompt(Long id) {
        Optional<Prompt> promptOpt = promptRepository.findById(id);
        
        if (promptOpt.isEmpty()) {
            log.warn("Prompt with ID {} not found for processing", id);
            return Optional.empty();
        }
        
        Prompt prompt = promptOpt.get();
        if (!"PENDING".equals(prompt.getStatus())) {
            log.info("Prompt {} is not in PENDING status, current status: {}", id, prompt.getStatus());
            return Optional.of(PromptResponse.fromEntity(prompt));
        }
        
        try {
            log.info("Processing prompt {}: {}", id, prompt.getContent());
            
            // Process the prompt with Gemini API
            String researchResult = geminiService.processResearchPrompt(prompt.getContent());
            
            // Update the prompt with the result
            prompt.setResponse(researchResult);
            prompt.setStatus("COMPLETED");
            prompt.setProcessedAt(LocalDateTime.now());
            
            Prompt savedPrompt = promptRepository.save(prompt);
            log.info("Prompt {} processed successfully", id);
            
            // Send notifications if requested
            sendNotifications(savedPrompt);
            
            return Optional.of(PromptResponse.fromEntity(savedPrompt));
        } catch (Exception e) {
            log.error("Error processing prompt {}: {}", id, e.getMessage());
            
            // Update prompt with error status
            prompt.setStatus("ERROR");
            prompt.setResponse("Error processing prompt: " + e.getMessage());
            Prompt savedPrompt = promptRepository.save(prompt);
            
            return Optional.of(PromptResponse.fromEntity(savedPrompt));
        }
    }
    
    /**
     * Process batch uploads of prompts.
     * 
     * @param prompts List of prompt contents
     * @param source Source of the prompts (e.g., "FILE", "SHEETS")
     * @param notificationEmail Email for notifications (optional)
     * @param notificationPhone Phone for notifications (optional)
     * @return Number of prompts created
     */
    @Transactional
    public int createBatchPrompts(List<String> prompts, String source, 
                                  String notificationEmail, String notificationPhone) {
        if (prompts == null || prompts.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (String content : prompts) {
            if (content != null && !content.trim().isEmpty()) {
                Prompt prompt = Prompt.builder()
                        .content(content.trim())
                        .status("PENDING")
                        .source(source)
                        .notificationEmail(notificationEmail)
                        .notificationPhone(notificationPhone)
                        .emailSent(false)
                        .smsSent(false)
                        .whatsappSent(false)
                        .build();
                
                promptRepository.save(prompt);
                count++;
            }
        }
        
        log.info("Created {} prompts from {} source", count, source);
        return count;
    }
    
    /**
     * Process all pending prompts.
     * This method is called by the scheduler.
     * 
     * @return Number of prompts processed
     */
    @Transactional
    public int processAllPendingPrompts() {
        List<Prompt> pendingPrompts = promptRepository.findPromptsToProcess();
        
        if (pendingPrompts.isEmpty()) {
            log.info("No pending prompts to process");
            return 0;
        }
        
        log.info("Processing {} pending prompts", pendingPrompts.size());
        int count = 0;
        
        for (Prompt prompt : pendingPrompts) {
            try {
                // Process the prompt with Gemini API
                String researchResult = geminiService.processResearchPrompt(prompt.getContent());
                
                // Update the prompt with the result
                prompt.setResponse(researchResult);
                prompt.setStatus("COMPLETED");
                prompt.setProcessedAt(LocalDateTime.now());
                
                promptRepository.save(prompt);
                
                // Send notifications if requested
                sendNotifications(prompt);
                
                count++;
                log.info("Processed pending prompt {}", prompt.getId());
            } catch (Exception e) {
                log.error("Error processing pending prompt {}: {}", prompt.getId(), e.getMessage());
                
                // Update prompt with error status
                prompt.setStatus("ERROR");
                prompt.setResponse("Error processing prompt: " + e.getMessage());
                promptRepository.save(prompt);
            }
        }
        
        log.info("Completed processing {} out of {} pending prompts", count, pendingPrompts.size());
        return count;
    }
    
    /**
     * Send notifications for a completed prompt.
     * 
     * @param prompt The processed prompt
     */
    private void sendNotifications(Prompt prompt) {
        // Only send notifications for completed prompts
        if (!"COMPLETED".equals(prompt.getStatus())) {
            return;
        }
        
        // Send email notification if requested and not already sent
        if (prompt.getNotificationEmail() != null && !prompt.getEmailSent()) {
            boolean emailSent = emailService.sendResearchReport(
                    prompt.getNotificationEmail(),
                    "Your Research Report is Ready",
                    prompt.getContent(),
                    prompt.getResponse()
            );
            
            if (emailSent) {
                prompt.setEmailSent(true);
                promptRepository.save(prompt);
            }
        }
        
        // Send SMS notification if requested and not already sent
        if (prompt.getNotificationPhone() != null && !prompt.getSmsSent()) {
            boolean smsSent = twilioService.sendSms(
                    prompt.getNotificationPhone(),
                    "Your research report for prompt '" + truncateString(prompt.getContent(), 50) + 
                    "' is now ready. Please check your email or the web dashboard to view it."
            );
            
            if (smsSent) {
                prompt.setSmsSent(true);
                promptRepository.save(prompt);
            }
        }
        
        // Send WhatsApp notification if requested and not already sent
        if (prompt.getNotificationPhone() != null && !prompt.getWhatsappSent()) {
            boolean whatsappSent = twilioService.sendWhatsApp(
                    prompt.getNotificationPhone(),
                    "Your research report for prompt '" + truncateString(prompt.getContent(), 50) + 
                    "' is now ready. Please check your email or the web dashboard to view it."
            );
            
            if (whatsappSent) {
                prompt.setWhatsappSent(true);
                promptRepository.save(prompt);
            }
        }
    }
    
    /**
     * Helper method to truncate a string to a maximum length.
     * 
     * @param input The input string
     * @param maxLength The maximum length
     * @return The truncated string
     */
    private String truncateString(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
}