package com.gemini.deepresearch.service;

import com.gemini.deepresearch.model.ApiConfig;
import com.gemini.deepresearch.repository.ApiConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Optional;

/**
 * Service for sending notifications via various channels.
 * This implementation uses open-source alternatives to proprietary services.
 */
@Service
@Slf4j
public class NotificationService {

    @Autowired
    private ApiConfigRepository apiConfigRepository;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Send SMS notification using SignalWire (open source Twilio alternative).
     * 
     * @param to The recipient phone number (with country code)
     * @param message The message to send
     * @return True if the message was sent successfully, false otherwise
     */
    public boolean sendSms(String to, String message) {
        Optional<ApiConfig> smsConfig = apiConfigRepository.findByConfigType(ApiConfig.ConfigType.SMS);
        
        if (smsConfig.isEmpty() || !isSmsConfigComplete(smsConfig.get())) {
            log.warn("SMS configuration is incomplete. Cannot send SMS.");
            return false;
        }
        
        ApiConfig config = smsConfig.get();
        String projectId = config.getProjectId();
        String spaceUrl = config.getSpaceUrl();
        String apiToken = config.getApiToken();
        String fromNumber = config.getFromNumber();
        
        if (projectId == null || spaceUrl == null || apiToken == null || fromNumber == null) {
            log.warn("SMS configuration is incomplete. Missing required fields.");
            return false;
        }
        
        try {
            // SignalWire API endpoint
            String url = "https://" + spaceUrl + "/api/laml/2010-04-01/Accounts/" + 
                        projectId + "/Messages.json";
            
            // Set up headers with Basic Authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = projectId + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            // Set up request parameters
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("From", fromNumber);
            requestBody.add("To", to);
            requestBody.add("Body", message);
            
            // Send request
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to {}", to);
                return true;
            } else {
                log.error("Failed to send SMS. Status code: {}", response.getStatusCodeValue());
                return false;
            }
            
        } catch (RestClientException e) {
            log.error("Error sending SMS via SignalWire: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send WhatsApp notification (using the same SignalWire API).
     * 
     * @param to The recipient phone number (with country code)
     * @param message The message to send
     * @return True if the message was sent successfully, false otherwise
     */
    public boolean sendWhatsApp(String to, String message) {
        // SignalWire and most open source alternatives don't have direct WhatsApp integration
        // For demonstration, we'll log the intention but actual implementation would require
        // other services like MessageBird, Infobip, etc.
        log.info("WhatsApp message would be sent to {} with content: {}", to, message);
        return false;
    }
    
    /**
     * Send email notification.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param text The email body
     * @return True if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("Email service is not configured. Cannot send email.");
            return false;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            Optional<ApiConfig> emailConfig = apiConfigRepository.findByConfigType(ApiConfig.ConfigType.EMAIL);
            if (emailConfig.isPresent()) {
                message.setFrom(emailConfig.get().getUsername());
            }
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
            return true;
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if SMS configuration is complete.
     * 
     * @param config The SMS configuration
     * @return True if the configuration is complete, false otherwise
     */
    private boolean isSmsConfigComplete(ApiConfig config) {
        return config != null && 
               config.getProjectId() != null && !config.getProjectId().isBlank() &&
               config.getSpaceUrl() != null && !config.getSpaceUrl().isBlank() &&
               config.getApiToken() != null && !config.getApiToken().isBlank() &&
               config.getFromNumber() != null && !config.getFromNumber().isBlank();
    }
    
    /**
     * Check if email configuration is complete.
     * 
     * @return True if the configuration is complete, false otherwise
     */
    public boolean isEmailConfigured() {
        return mailSender != null && 
               apiConfigRepository.findByConfigType(ApiConfig.ConfigType.EMAIL).isPresent();
    }
    
    /**
     * Check if SMS configuration is complete.
     * 
     * @return True if the configuration is complete, false otherwise
     */
    public boolean isSmsConfigured() {
        Optional<ApiConfig> config = apiConfigRepository.findByConfigType(ApiConfig.ConfigType.SMS);
        return config.isPresent() && isSmsConfigComplete(config.get());
    }
}