package com.smarthire.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean; // REQUIRED for @Bean
import org.springframework.context.annotation.Configuration;

@Configuration 
public class MongoConfig {
    
    // THIS METHOD IS REQUIRED. It uses the MONGODB_URI set in your environment variables.
    @Bean
    public MongoClient mongoClient() {
        // We assume your deployment tool (Render) sets the variable MONGODB_URI
        // which contains the full connection string.
        String mongoUri = System.getenv("MONGODB_URI");
        if (mongoUri == null || mongoUri.isEmpty()) {
            throw new RuntimeException("MONGODB_URI environment variable not set or empty.");
        }
        return MongoClients.create(mongoUri); 
    }
}
