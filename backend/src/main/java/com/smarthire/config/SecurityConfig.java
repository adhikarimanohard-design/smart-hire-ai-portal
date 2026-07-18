package com.smarthire.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    /**
     * Registered with HIGHEST_PRECEDENCE so it runs before Spring Security
     * and before multipart/file-upload parsing. This guarantees CORS
     * headers are present on every response — including ones where an
     * exception is thrown while parsing the resume upload, before the
     * request ever reaches a controller's try/catch. Without this, a
     * multipart-parsing failure comes back with no
     * Access-Control-Allow-Origin header, and the browser reports it as a
     * bare "Failed to fetch" instead of the real error.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> bean =
            new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // NOTE: CORS is handled entirely by the CorsFilter bean above
            // (registered at HIGHEST_PRECEDENCE). Do NOT also call
            // .cors(...) here — Spring Security would attach a second
            // Access-Control-Allow-Origin header, and browsers reject
            // responses with duplicate CORS headers outright, which is
            // why every request started failing instantly.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/jobs").permitAll()
                .requestMatchers("/api/jobs/search").permitAll()
                .requestMatchers("/api/applications/user/**").permitAll()
                .requestMatchers("/api/jobs/recommendations/**").permitAll()
                .requestMatchers("/api/recruiter/**").permitAll()
                .requestMatchers("/api/interviews/**").permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
