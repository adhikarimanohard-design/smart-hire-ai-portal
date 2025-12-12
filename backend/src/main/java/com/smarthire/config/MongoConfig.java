package com.smarthire.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class MongoConfig {
    
    @Bean
    public MongoClient mongoClient() {
        // This uses the MONGODB_URI variable you set on Render.
        return MongoClients.create(System.getenv("MONGODB_URI")); 
    }
}
