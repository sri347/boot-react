package com.gemini.deepresearch.service;

import com.gemini.deepresearch.dto.PromptRequest;
import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.dto.PromptTemplateDTO;
import com.gemini.deepresearch.dto.TemplateApplyRequest;
import com.gemini.deepresearch.model.PromptTemplate;
import com.gemini.deepresearch.repository.PromptTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing prompt templates.
 */
@Service
@Slf4j
public class TemplateService {
    
    @Autowired
    private PromptTemplateRepository templateRepository;
    
    @Autowired
    private PromptService promptService;
    
    /**
     * Get all templates.
     * 
     * @return A list of all templates
     */
    public List<PromptTemplateDTO> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(PromptTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get template by ID.
     * 
     * @param id The template ID
     * @return The template, if found
     */
    public Optional<PromptTemplateDTO> getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(PromptTemplateDTO::fromEntity);
    }
    
    /**
     * Save a template.
     * 
     * @param templateDTO The template to save
     * @return The saved template
     */
    public PromptTemplateDTO saveTemplate(PromptTemplateDTO templateDTO) {
        PromptTemplate template = templateDTO.toEntity();
        PromptTemplate savedTemplate = templateRepository.save(template);
        return PromptTemplateDTO.fromEntity(savedTemplate);
    }
    
    /**
     * Delete a template.
     * 
     * @param id The template ID
     */
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }
    
    /**
     * Search templates by name and/or category.
     * 
     * @param name The name to search for (optional)
     * @param category The category to filter by (optional)
     * @return A list of matching templates
     */
    public List<PromptTemplateDTO> searchTemplates(String name, String category) {
        List<PromptTemplate> templates;
        
        if (name != null && !name.isBlank() && category != null && !category.isBlank()) {
            templates = templateRepository.findByNameContainingIgnoreCaseAndCategory(name, category);
        } else if (name != null && !name.isBlank()) {
            templates = templateRepository.findByNameContainingIgnoreCase(name);
        } else if (category != null && !category.isBlank()) {
            templates = templateRepository.findByCategory(category);
        } else {
            templates = templateRepository.findAll();
        }
        
        return templates.stream()
                .map(PromptTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get public templates.
     * 
     * @return A list of public templates
     */
    public List<PromptTemplateDTO> getPublicTemplates() {
        return templateRepository.findByIsPublic(true).stream()
                .map(PromptTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get templates by creator.
     * 
     * @param createdBy The creator name
     * @return A list of templates created by the given creator
     */
    public List<PromptTemplateDTO> getTemplatesByCreator(String createdBy) {
        return templateRepository.findByCreatedBy(createdBy).stream()
                .map(PromptTemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Apply a template with variables and create a prompt.
     * 
     * @param request The template application request
     * @return The created prompt
     */
    public PromptResponse applyTemplate(TemplateApplyRequest request) {
        PromptTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + request.getTemplateId()));
        
        // Apply the variables to the template
        String promptContent = template.applyTemplate(request.getVariables());
        
        // Create a new prompt request
        PromptRequest promptRequest = PromptRequest.builder()
                .content(promptContent)
                .createdBy(template.getCreatedBy())
                .source("TEMPLATE")
                .notificationEmail(request.getNotificationEmail())
                .notificationPhone(request.getNotificationPhone())
                .sendSms(request.getSendSms() != null ? request.getSendSms() : false)
                .sendWhatsapp(request.getSendWhatsapp() != null ? request.getSendWhatsapp() : false)
                .build();
        
        // Increment the usage count
        template.setUsageCount(template.getUsageCount() != null ? template.getUsageCount() + 1 : 1);
        templateRepository.save(template);
        
        // Create the prompt
        return promptService.createPrompt(promptRequest);
    }
    
    /**
     * Get a list of all categories.
     * 
     * @return A list of all categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        
        templateRepository.findAll().forEach(template -> {
            if (template.getCategory() != null && !template.getCategory().isBlank() && !categories.contains(template.getCategory())) {
                categories.add(template.getCategory());
            }
        });
        
        return categories;
    }
    
    /**
     * Validate a template by checking if it contains valid placeholders.
     * 
     * @param templateContent The template content
     * @param placeholderFormat The placeholder format (optional)
     * @return A list of placeholders found in the template
     */
    public List<String> validateTemplate(String templateContent, String placeholderFormat) {
        PromptTemplate template = PromptTemplate.builder()
                .templateContent(templateContent)
                .placeholderFormat(placeholderFormat)
                .build();
        
        return template.extractPlaceholders();
    }
    
    /**
     * Preview a template by applying variables.
     * 
     * @param templateContent The template content
     * @param placeholderFormat The placeholder format (optional)
     * @param variables The variables to apply
     * @return The preview result
     */
    public String previewTemplate(String templateContent, String placeholderFormat, Map<String, String> variables) {
        PromptTemplate template = PromptTemplate.builder()
                .templateContent(templateContent)
                .placeholderFormat(placeholderFormat)
                .build();
        
        return template.applyTemplate(variables);
    }
}