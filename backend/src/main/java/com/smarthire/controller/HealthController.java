package com.smarthire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health/ping endpoint for uptime monitors (e.g. UptimeRobot).
 *
 * Spring MVC automatically handles HTTP HEAD requests for any @GetMapping
 * method, so this single GET handler covers both:
 *   - UptimeRobot "HTTP(s)" monitor set to HEAD (default)
 *   - Plain GET requests (curl, browser, etc.)
 *
 * Deliberately does NOT touch MongoDB or any service/repository layer,
 * so the ping is fast and doesn't burn DB connections just to keep the
 * container awake.
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
