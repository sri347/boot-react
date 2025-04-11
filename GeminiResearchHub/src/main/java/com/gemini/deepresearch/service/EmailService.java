package com.gemini.deepresearch.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending email notifications.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:}")
    private String emailFrom;
    
    /**
     * Send a simple email with plain text content.
     * 
     * @param to The recipient's email address
     * @param subject The email subject
     * @param text The email content (plain text)
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendSimpleEmail(String to, String subject, String text) {
        if (!isEmailServiceAvailable()) {
            log.warn("Cannot send email: Email service is not configured");
            return false;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            
            emailSender.send(message);
            log.info("Email sent to: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send an HTML email with the research report.
     * 
     * @param to The recipient's email address
     * @param subject The email subject
     * @param promptContent The original prompt content
     * @param researchResult The research result (report)
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendResearchReport(String to, String subject, String promptContent, String researchResult) {
        if (!isEmailServiceAvailable()) {
            log.warn("Cannot send email: Email service is not configured");
            return false;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Prepare the Thymeleaf context for the email template
            Context context = new Context();
            context.setVariable("prompt", promptContent);
            context.setVariable("result", researchResult);
            context.setVariable("timestamp", java.time.LocalDateTime.now().toString());
            
            // Process the template
            String htmlContent = templateEngine.process("email/research-report", context);
            helper.setText(htmlContent, true);
            
            // Add logo if available
            try {
                helper.addInline("logo", new ClassPathResource("static/images/logo.png"));
            } catch (Exception e) {
                log.warn("Logo image not found for email, continuing without it");
            }
            
            emailSender.send(message);
            log.info("Research report email sent to: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send research report email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the email service is available.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isEmailServiceAvailable() {
        return emailFrom != null && !emailFrom.isEmpty();
    }
}