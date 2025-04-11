package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.dto.PromptTemplateDTO;
import com.gemini.deepresearch.dto.TemplateApplyRequest;
import com.gemini.deepresearch.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controller for template-related endpoints.
 */
@RestController
@RequestMapping("/api/templates")
@Slf4j
public class TemplateController {

    @Autowired
    private TemplateService templateService;
    
    /**
     * Get all templates.
     * 
     * @return A list of all templates
     */
    @GetMapping
    public ResponseEntity<List<PromptTemplateDTO>> getAllTemplates() {
        log.info("Get all templates request received");
        List<PromptTemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get a template by ID.
     * 
     * @param id The template ID
     * @return The template, if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplateDTO> getTemplateById(@PathVariable Long id) {
        log.info("Get template by ID request received: {}", id);
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found with ID: " + id));
    }
    
    /**
     * Create a new template.
     * 
     * @param template The template to create
     * @return The created template
     */
    @PostMapping
    public ResponseEntity<PromptTemplateDTO> createTemplate(@RequestBody PromptTemplateDTO template) {
        log.info("Create template request received: {}", template.getName());
        PromptTemplateDTO createdTemplate = templateService.saveTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }
    
    /**
     * Update an existing template.
     * 
     * @param id The template ID
     * @param template The updated template
     * @return The updated template
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplateDTO> updateTemplate(@PathVariable Long id, @RequestBody PromptTemplateDTO template) {
        log.info("Update template request received for ID: {}", id);
        
        if (!templateService.getTemplateById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found with ID: " + id);
        }
        
        template.setId(id);
        PromptTemplateDTO updatedTemplate = templateService.saveTemplate(template);
        return ResponseEntity.ok(updatedTemplate);
    }
    
    /**
     * Delete a template.
     * 
     * @param id The template ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("Delete template request received for ID: {}", id);
        
        if (!templateService.getTemplateById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found with ID: " + id);
        }
        
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Search templates by name and/or category.
     * 
     * @param name The name to search for (optional)
     * @param category The category to filter by (optional)
     * @return A list of matching templates
     */
    @GetMapping("/search")
    public ResponseEntity<List<PromptTemplateDTO>> searchTemplates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        
        log.info("Search templates request received. Name: {}, Category: {}", name, category);
        List<PromptTemplateDTO> templates = templateService.searchTemplates(name, category);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get public templates.
     * 
     * @return A list of public templates
     */
    @GetMapping("/public")
    public ResponseEntity<List<PromptTemplateDTO>> getPublicTemplates() {
        log.info("Get public templates request received");
        List<PromptTemplateDTO> templates = templateService.getPublicTemplates();
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get templates by creator.
     * 
     * @param createdBy The creator name
     * @return A list of templates created by the given creator
     */
    @GetMapping("/creator/{createdBy}")
    public ResponseEntity<List<PromptTemplateDTO>> getTemplatesByCreator(@PathVariable String createdBy) {
        log.info("Get templates by creator request received: {}", createdBy);
        List<PromptTemplateDTO> templates = templateService.getTemplatesByCreator(createdBy);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Apply a template with variables and create a prompt.
     * 
     * @param request The template application request
     * @return The created prompt
     */
    @PostMapping("/apply")
    public ResponseEntity<PromptResponse> applyTemplate(@RequestBody TemplateApplyRequest request) {
        log.info("Apply template request received for template ID: {}", request.getTemplateId());
        PromptResponse response = templateService.applyTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get a list of all categories.
     * 
     * @return A list of all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("Get all categories request received");
        List<String> categories = templateService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Validate a template by checking if it contains valid placeholders.
     * 
     * @param templateContent The template content
     * @param placeholderFormat The placeholder format (optional)
     * @return A list of placeholders found in the template
     */
    @PostMapping("/validate")
    public ResponseEntity<List<String>> validateTemplate(
            @RequestParam String templateContent,
            @RequestParam(required = false) String placeholderFormat) {
        
        log.info("Validate template request received");
        List<String> placeholders = templateService.validateTemplate(templateContent, placeholderFormat);
        return ResponseEntity.ok(placeholders);
    }
    
    /**
     * Preview a template by applying variables.
     * 
     * @param templateContent The template content
     * @param placeholderFormat The placeholder format (optional)
     * @param variables The variables to apply
     * @return The preview result
     */
    @PostMapping("/preview")
    public ResponseEntity<String> previewTemplate(
            @RequestParam String templateContent,
            @RequestParam(required = false) String placeholderFormat,
            @RequestBody Map<String, String> variables) {
        
        log.info("Preview template request received");
        String preview = templateService.previewTemplate(templateContent, placeholderFormat, variables);
        return ResponseEntity.ok(preview);
    }
}