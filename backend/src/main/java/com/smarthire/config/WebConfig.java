package com.smarthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS handled in SecurityConfig.java
    // Keep this empty to avoid Wildcard crash
}
