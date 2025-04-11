package com.gemini.deepresearch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a reusable prompt template.
 */
@Entity
@Table(name = "prompt_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 5000)
    private String templateContent;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "placeholder_format")
    private String placeholderFormat;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "usage_count")
    private Integer usageCount;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * Apply variables to the template to create a filled-in prompt.
     * 
     * @param variables The variables to fill in the template
     * @return The filled-in prompt
     */
    public String applyTemplate(java.util.Map<String, String> variables) {
        String result = this.templateContent;
        
        // If no placeholder format is specified, default to {{variable}}
        String format = this.placeholderFormat != null ? this.placeholderFormat : "{{%s}}";
        
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = String.format(format, entry.getKey());
            result = result.replace(placeholder, entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Extract placeholder names from the template content.
     * 
     * @return A list of placeholder names
     */
    public java.util.List<String> extractPlaceholders() {
        java.util.List<String> placeholders = new java.util.ArrayList<>();
        
        // If no placeholder format is specified, default to {{variable}}
        String format = this.placeholderFormat != null ? this.placeholderFormat : "{{%s}}";
        
        // Extract the pattern part (e.g., for "{{%s}}" it would extract "{{" and "}}")
        String[] parts = format.split("%s");
        if (parts.length != 2) {
            return placeholders; // Invalid format
        }
        
        String prefix = parts[0];
        String suffix = parts[1];
        
        // Find all placeholders in the template
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                java.util.regex.Pattern.quote(prefix) + "([^" + 
                java.util.regex.Pattern.quote(suffix.substring(0, 1)) + "]+)" + 
                java.util.regex.Pattern.quote(suffix)
        );
        java.util.regex.Matcher matcher = pattern.matcher(this.templateContent);
        
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        
        return placeholders;
    }
}