package com.smarthire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
public class SmartHireApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);

        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════╗\n" +
            "║                                                           ║\n" +
            "║        🚀 SMART HIRE Backend Started Successfully! 🚀     ║\n" +
            "║                                                           ║\n" +
            "║   📡 API Base:     /api                                   ║\n" +
            "║   💊 Health:       /actuator/health  ← UptimeRobot URL   ║\n" +
            "║   💊 Alt Health:   /health           ← backup ping URL   ║\n" +
            "║   🔒 MongoDB Connected                                    ║\n" +
            "║   🤖 AI Matching Ready                                    ║\n" +
            "║                                                           ║\n" +
            "╚═══════════════════════════════════════════════════════════╝\n"
        );
    }

    /**
     * Simple /health endpoint as a backup ping target for UptimeRobot.
     * Use either:
     *   https://your-render-url.onrender.com/actuator/health   (preferred)
     *   https://your-render-url.onrender.com/health            (backup)
     *
     * Both return HTTP 200 when the app is running.
     */
    @RestController
    static class HealthController {
        @GetMapping("/health")
        public Map<String, String> health() {
            return Map.of("status", "UP", "service", "SmartHire Backend");
        }
    }
}
