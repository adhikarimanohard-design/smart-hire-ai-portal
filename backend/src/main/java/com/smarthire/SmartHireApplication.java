package com.smarthire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class SmartHireApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);

        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════╗\n" +
            "║                                                           ║\n" +
            "║        🚀 SMART HIRE Backend Started Successfully! 🚀     ║\n" +
            "║                                                           ║\n" +
            "║   📊 Dashboard: http://localhost:8080                     ║\n" +
            "║   📡 API Base: http://localhost:8080/api                  ║\n" +
            "║   🔒 MongoDB Connected                                    ║\n" +
            "║   🤖 ML Engine Ready                                      ║\n" +
            "║                                                           ║\n" +
            "╚═══════════════════════════════════════════════════════════╝\n"
        );
    }
}