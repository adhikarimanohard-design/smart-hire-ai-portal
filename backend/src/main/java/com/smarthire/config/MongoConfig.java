package com.smarthire.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class MongoConfig {
    
    // This bean uses the MONGODB_URI environment variable set on Render.
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(System.getenv("MONGODB_URI")); 
    }
}