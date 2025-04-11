package com.gemini.deepresearch.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS and WhatsApp notifications using Twilio.
 */
@Service
@Slf4j
public class TwilioService {

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    private boolean isInitialized = false;
    
    /**
     * Initialize the Twilio client with credentials.
     * This method is called lazily when the service is first used.
     * 
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeTwilio() {
        if (isInitialized) {
            return true;
        }
        
        if (twilioAccountSid == null || twilioAccountSid.isEmpty() ||
            twilioAuthToken == null || twilioAuthToken.isEmpty() ||
            twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
            log.warn("Twilio credentials are not set. SMS and WhatsApp features are disabled.");
            return false;
        }
        
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            isInitialized = true;
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize Twilio client: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send an SMS notification to the specified phone number.
     * 
     * @param to The recipient's phone number
     * @param body The message body
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendSms(String to, String body) {
        if (!initializeTwilio()) {
            log.warn("Cannot send SMS: Twilio is not initialized");
            return false;
        }
        
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    body)
                .create();
                
            log.info("SMS sent with SID: {}", message.getSid());
            return true;
        } catch (ApiException e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send a WhatsApp notification to the specified phone number.
     * 
     * @param to The recipient's phone number
     * @param body The message body
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendWhatsApp(String to, String body) {
        if (!initializeTwilio()) {
            log.warn("Cannot send WhatsApp message: Twilio is not initialized");
            return false;
        }
        
        try {
            // Format WhatsApp number with whatsapp: prefix
            String whatsappFrom = "whatsapp:" + twilioPhoneNumber;
            String whatsappTo = "whatsapp:" + to;
            
            Message message = Message.creator(
                    new PhoneNumber(whatsappTo),
                    new PhoneNumber(whatsappFrom),
                    body)
                .create();
                
            log.info("WhatsApp message sent with SID: {}", message.getSid());
            return true;
        } catch (ApiException e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the Twilio SMS service is available.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isSmsAvailable() {
        return initializeTwilio();
    }
    
    /**
     * Check if the Twilio WhatsApp service is available.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isWhatsAppAvailable() {
        return initializeTwilio();
    }
}