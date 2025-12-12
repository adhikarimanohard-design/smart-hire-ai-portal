package com.smarthire.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean; // New Import
import org.springframework.context.annotation.Configuration;

@Configuration // <--- KEEP THIS
public class MongoConfig {
    
    // Create the MongoClient bean using the environment variable
    @Bean
    public MongoClient mongoClient() {
        // This uses the MONGODB_URI variable you set on Render.
        return MongoClients.create(System.getenv("MONGODB_URI")); 
    }
    
    // You do not need getDatabaseName(), autoIndexCreation(), or AbstractMongoClientConfiguration.
}