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
 * Entity representing a research prompt.
 */
@Entity
@Table(name = "prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String result;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PromptStatus status;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(nullable = false)
    private String source; // WEB, FILE, SHEETS
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "notification_email")
    private String notificationEmail;
    
    @Column(name = "notification_phone")
    private String notificationPhone;
    
    @Column(name = "send_sms")
    private Boolean sendSms;
    
    @Column(name = "send_whatsapp")
    private Boolean sendWhatsapp;
    
    @Column(name = "notification_sent")
    private Boolean notificationSent;
    
    /**
     * Enumeration of possible prompt statuses.
     */
    public enum PromptStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        ERROR
    }
}