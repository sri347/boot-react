package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.ApiStatusResponse;
import com.gemini.deepresearch.dto.PromptResponse;
import com.gemini.deepresearch.service.ApiConfigService;
import com.gemini.deepresearch.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Controller for web pages.
 */
@Controller
@Slf4j
public class WebController {

    @Autowired
    private PromptService promptService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    /**
     * Home page.
     * 
     * @param model The model for the view
     * @return The home page view
     */
    @GetMapping("/")
    public String home(Model model) {
        log.info("Home page requested");
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "index";
    }
    
    /**
     * Admin dashboard page.
     * 
     * @param model The model for the view
     * @return The admin dashboard view
     */
    @GetMapping("/admin")
    public String admin(Model model) {
        log.info("Admin page requested");
        
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
     * View a specific prompt.
     * 
     * @param id The prompt ID
     * @param model The model for the view
     * @return The prompt detail view
     */
    @GetMapping("/prompts/{id}")
    public String viewPrompt(@PathVariable Long id, Model model) {
        log.info("View prompt page requested for ID: {}", id);
        
        // Get the prompt
        PromptResponse prompt = promptService.getPromptById(id)
                .orElse(null);
        
        if (prompt == null) {
            model.addAttribute("error", "Prompt not found");
            return "error";
        }
        
        model.addAttribute("prompt", prompt);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "prompt-detail";
    }
}