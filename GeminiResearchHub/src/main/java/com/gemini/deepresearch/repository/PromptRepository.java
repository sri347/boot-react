package com.gemini.deepresearch.repository;

import com.gemini.deepresearch.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for prompt entities.
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    /**
     * Find prompts by status.
     * 
     * @param status The status to filter by
     * @return A list of prompts with the given status
     */
    List<Prompt> findByStatus(Prompt.PromptStatus status);
    
    /**
     * Find prompts by status ordered by creation date (newest first).
     * 
     * @param status The status to filter by
     * @return A list of prompts with the given status, ordered by creation date
     */
    List<Prompt> findByStatusOrderByCreatedAtDesc(Prompt.PromptStatus status);
    
    /**
     * Find prompts by source.
     * 
     * @param source The source to filter by
     * @return A list of prompts with the given source
     */
    List<Prompt> findBySource(String source);
    
    /**
     * Find prompts by creator.
     * 
     * @param createdBy The creator name to filter by
     * @return A list of prompts created by the given creator
     */
    List<Prompt> findByCreatedBy(String createdBy);
    
    /**
     * Find prompts with notification pending.
     * 
     * @return A list of prompts that need notification
     */
    List<Prompt> findByStatusAndNotificationSent(Prompt.PromptStatus status, Boolean notificationSent);
}