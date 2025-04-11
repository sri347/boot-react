package com.gemini.deepresearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

/**
 * Application configuration class for setting up beans and configurations.
 */
@Configuration
public class AppConfig {

    /**
     * Configures a RestTemplate bean for making HTTP requests.
     * @return A configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Configures a ThreadPoolTaskScheduler for scheduled tasks.
     * @return A configured ThreadPoolTaskScheduler
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("Task-Scheduler-");
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * Configures the JavaMailSender for sending emails.
     * @return A configured JavaMailSenderImpl
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        
        // Credentials will be picked up from environment variables in application.properties
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}