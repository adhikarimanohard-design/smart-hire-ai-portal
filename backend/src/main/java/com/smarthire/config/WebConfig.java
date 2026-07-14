package com.smarthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS is now handled in SecurityConfig.java
    // Keep this file empty of CORS rules to avoid the "Wildcard" crash.
}
