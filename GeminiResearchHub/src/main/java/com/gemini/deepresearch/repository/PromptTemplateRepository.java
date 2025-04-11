package com.gemini.deepresearch.repository;

import com.gemini.deepresearch.model.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for prompt template entities.
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    
    /**
     * Find templates by name (containing the search string, case-insensitive).
     * 
     * @param name The name to search for
     * @return A list of templates with matching names
     */
    List<PromptTemplate> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find templates by category.
     * 
     * @param category The category to filter by
     * @return A list of templates in the given category
     */
    List<PromptTemplate> findByCategory(String category);
    
    /**
     * Find templates by creator.
     * 
     * @param createdBy The creator name to filter by
     * @return A list of templates created by the given creator
     */
    List<PromptTemplate> findByCreatedBy(String createdBy);
    
    /**
     * Find public templates.
     * 
     * @return A list of public templates
     */
    List<PromptTemplate> findByIsPublic(Boolean isPublic);
    
    /**
     * Find templates by name and category.
     * 
     * @param name The name to search for
     * @param category The category to filter by
     * @return A list of templates with matching names in the given category
     */
    List<PromptTemplate> findByNameContainingIgnoreCaseAndCategory(String name, String category);
}