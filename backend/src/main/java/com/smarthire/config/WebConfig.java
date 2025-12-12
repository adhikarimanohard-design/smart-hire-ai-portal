package com.smarthire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // IMPORTANT: Reading from the new environment variable set on Render
    @Value("${CORS_ALLOWED_ORIGINS}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        
        // This splits your comma-separated string (e.g., frontend.com,localhost:3000)
        String[] origins = allowedOrigins.split(",");

        registry.addMapping("/**") // Maps to all endpoints, including /api
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
