package com.smarthire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())       // Disable CSRF
            .cors(cors -> {})                   // Enable CORS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Allow all /api/** endpoints
                .anyRequest().authenticated()           // Secure other endpoints
            )
            .httpBasic(); // Optional: use basic auth for secured endpoints

        return http.build();
    }
}