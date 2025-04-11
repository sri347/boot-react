package com.gemini.deepresearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application entry point for the Gemini Deep Research application.
 * Enables Spring Boot auto-configuration and scheduled tasks.
 */
@SpringBootApplication
@EnableScheduling
public class DeepResearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepResearchApplication.class, args);
    }
}