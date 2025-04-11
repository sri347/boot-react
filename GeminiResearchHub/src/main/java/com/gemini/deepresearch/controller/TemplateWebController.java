package com.gemini.deepresearch.controller;

import com.gemini.deepresearch.dto.ApiStatusResponse;
import com.gemini.deepresearch.dto.PromptTemplateDTO;
import com.gemini.deepresearch.service.ApiConfigService;
import com.gemini.deepresearch.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for template web pages.
 */
@Controller
@RequestMapping("/templates")
@Slf4j
public class TemplateWebController {

    @Autowired
    private TemplateService templateService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    /**
     * Templates listing page.
     * 
     * @param model The model for the view
     * @return The templates page view
     */
    @GetMapping
    public String templatesPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            Model model) {
        
        log.info("Templates page requested");
        
        // Get templates based on search params
        List<PromptTemplateDTO> templates;
        if (name != null || category != null) {
            templates = templateService.searchTemplates(name, category);
        } else {
            templates = templateService.getAllTemplates();
        }
        model.addAttribute("templates", templates);
        
        // Get all categories for the dropdown
        List<String> categories = templateService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // Add search params to model for form values
        model.addAttribute("searchName", name);
        model.addAttribute("searchCategory", category);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "templates/list";
    }
    
    /**
     * Template creation page.
     * 
     * @param model The model for the view
     * @return The template creation page view
     */
    @GetMapping("/create")
    public String createTemplatePage(Model model) {
        log.info("Create template page requested");
        
        // Add empty template for the form
        model.addAttribute("template", new PromptTemplateDTO());
        
        // Get all categories for the dropdown
        List<String> categories = templateService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "templates/edit";
    }
    
    /**
     * Template edit page.
     * 
     * @param id The template ID
     * @param model The model for the view
     * @return The template edit page view
     */
    @GetMapping("/edit/{id}")
    public String editTemplatePage(@PathVariable Long id, Model model) {
        log.info("Edit template page requested for ID: {}", id);
        
        // Get the template
        PromptTemplateDTO template = templateService.getTemplateById(id)
                .orElse(null);
        
        if (template == null) {
            model.addAttribute("error", "Template not found");
            return "error";
        }
        
        model.addAttribute("template", template);
        
        // Get all categories for the dropdown
        List<String> categories = templateService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "templates/edit";
    }
    
    /**
     * Save template.
     * 
     * @param template The template to save
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to templates page
     */
    @PostMapping("/save")
    public String saveTemplate(@ModelAttribute PromptTemplateDTO template, RedirectAttributes redirectAttributes) {
        log.info("Save template request received for name: {}", template.getName());
        
        try {
            PromptTemplateDTO savedTemplate = templateService.saveTemplate(template);
            redirectAttributes.addFlashAttribute("message", "Template saved successfully!");
            return "redirect:/templates";
        } catch (Exception e) {
            log.error("Error saving template: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error saving template: " + e.getMessage());
            return "redirect:/templates/create";
        }
    }
    
    /**
     * Delete template.
     * 
     * @param id The template ID
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to templates page
     */
    @PostMapping("/delete/{id}")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Delete template request received for ID: {}", id);
        
        try {
            templateService.deleteTemplate(id);
            redirectAttributes.addFlashAttribute("message", "Template deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting template: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting template: " + e.getMessage());
        }
        
        return "redirect:/templates";
    }
    
    /**
     * Template use page.
     * 
     * @param id The template ID
     * @param model The model for the view
     * @return The template use page view
     */
    @GetMapping("/use/{id}")
    public String useTemplatePage(@PathVariable Long id, Model model) {
        log.info("Use template page requested for ID: {}", id);
        
        // Get the template
        PromptTemplateDTO template = templateService.getTemplateById(id)
                .orElse(null);
        
        if (template == null) {
            model.addAttribute("error", "Template not found");
            return "error";
        }
        
        model.addAttribute("template", template);
        
        // Get API status for the view
        ApiStatusResponse apiStatus = apiConfigService.getApiStatus();
        model.addAttribute("apiStatus", apiStatus);
        
        return "templates/use";
    }
}