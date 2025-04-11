package com.gemini.deepresearch.dto;

import com.gemini.deepresearch.model.PromptTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for prompt templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateDTO {
    
    private Long id;
    
    private String name;
    
    private String templateContent;
    
    private String description;
    
    private String placeholderFormat;
    
    private String createdBy;
    
    private Boolean isPublic;
    
    private String category;
    
    private Integer usageCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<String> placeholders;
    
    /**
     * Convert entity to DTO.
     * 
     * @param template The entity
     * @return The DTO
     */
    public static PromptTemplateDTO fromEntity(PromptTemplate template) {
        return PromptTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .templateContent(template.getTemplateContent())
                .description(template.getDescription())
                .placeholderFormat(template.getPlaceholderFormat())
                .createdBy(template.getCreatedBy())
                .isPublic(template.getIsPublic())
                .category(template.getCategory())
                .usageCount(template.getUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .placeholders(template.extractPlaceholders())
                .build();
    }
    
    /**
     * Convert DTO to entity.
     * 
     * @return The entity
     */
    public PromptTemplate toEntity() {
        return PromptTemplate.builder()
                .id(this.id)
                .name(this.name)
                .templateContent(this.templateContent)
                .description(this.description)
                .placeholderFormat(this.placeholderFormat)
                .createdBy(this.createdBy)
                .isPublic(this.isPublic)
                .category(this.category)
                .usageCount(this.usageCount != null ? this.usageCount : 0)
                .build();
    }
}