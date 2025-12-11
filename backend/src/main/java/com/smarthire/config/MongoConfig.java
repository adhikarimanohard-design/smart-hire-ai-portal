package com.smarthire.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.smarthire.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "smarthire"; // your DB name
    }

    // REQUIRED override method
    @Override
    public MongoClient mongoClient() {
        // Use your MongoDB Atlas connection string here
        return MongoClients.create(System.getenv("MONGODB_URI"));
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}