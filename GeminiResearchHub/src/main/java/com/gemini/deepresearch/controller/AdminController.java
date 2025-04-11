package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.ApiConfigDTO;
import com.gemini.deepresearch.dto.ApiStatusResponse;
import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.model.ApiConfig;
import com.gemini.deepresearch.service.ApiConfigService;
import com.gemini.deepresearch.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller for admin pages and API configuration.
 */
@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private ApiConfigService apiConfigService;
    
    @Autowired
    private PromptService promptService;
    
    /**
     * Admin dashboard page.
     * 
     * @param model The model for the view
     * @return The admin dashboard view
     */
    @GetMapping("")
    public String adminDashboard(Model model) {
        log.info("Admin dashboard page requested");
        
        // Get all prompts for the dashboard
        List<PromptResponse> allPrompts = promptService.getAllPrompts();
        model.addAttribute("prompts", allPrompts);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        // Add counts for the dashboard
        long totalPrompts = allPrompts.size();
        long pendingPrompts = allPrompts.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        long completedPrompts = allPrompts.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long errorPrompts = allPrompts.stream().filter(p -> "ERROR".equals(p.getStatus())).count();
        
        model.addAttribute("totalPrompts", totalPrompts);
        model.addAttribute("pendingPrompts", pendingPrompts);
        model.addAttribute("completedPrompts", completedPrompts);
        model.addAttribute("errorPrompts", errorPrompts);
        
        return "admin";
    }
    
    /**
     * API Configuration page.
     * 
     * @param model The model for the view
     * @return The API configuration view
     */
    @GetMapping("/api-config")
    public String apiConfigPage(Model model) {
        log.info("API configuration page requested");
        
        // Get all API configurations
        List<ApiConfigDTO> configs = apiConfigService.getAllConfigs();
        model.addAttribute("configs", configs);
        
        // Add empty config for the form
        model.addAttribute("newConfig", new ApiConfigDTO());
        
        // Add config types for the dropdown
        model.addAttribute("configTypes", ApiConfig.ConfigType.values());
        
        return "api-config";
    }
    
    /**
     * Save API configuration.
     * 
     * @param config The API configuration to save
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to API configuration page
     */
    @PostMapping("/api-config/save")
    public String saveApiConfig(@ModelAttribute ApiConfigDTO config, RedirectAttributes redirectAttributes) {
        log.info("Save API configuration request received for type: {}", config.getConfigType());
        
        try {
            apiConfigService.saveConfig(config);
            redirectAttributes.addFlashAttribute("message", "Configuration saved successfully!");
        } catch (Exception e) {
            log.error("Error saving API configuration: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error saving configuration: " + e.getMessage());
        }
        
        return "redirect:/admin/api-config";
    }
    
    /**
     * Get API configuration by ID.
     * 
     * @param id The configuration ID
     * @return The configuration, if found
     */
    @GetMapping("/api-config/{id}")
    @ResponseBody
    public ResponseEntity<?> getApiConfig(@PathVariable Long id) {
        log.info("Get API configuration request received for ID: {}", id);
        
        return apiConfigService.getConfigById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete API configuration.
     * 
     * @param id The configuration ID
     * @return Success or error response
     */
    @DeleteMapping("/api-config/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteApiConfig(@PathVariable Long id) {
        log.info("Delete API configuration request received for ID: {}", id);
        
        try {
            apiConfigService.deleteConfig(id);
            return ResponseEntity.ok(Map.of("message", "Configuration deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting API configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting configuration: " + e.getMessage()));
        }
    }
    
    /**
     * Test API configuration.
     * 
     * @param id The configuration ID
     * @return Test result
     */
    @PostMapping("/api-config/{id}/test")
    @ResponseBody
    public ResponseEntity<?> testApiConfig(@PathVariable Long id) {
        log.info("Test API configuration request received for ID: {}", id);
        
        try {
            boolean success = apiConfigService.testConfig(id);
            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "Configuration test successful!" : "Configuration test failed"
            ));
        } catch (Exception e) {
            log.error("Error testing API configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error testing configuration: " + e.getMessage()));
        }
    }
}