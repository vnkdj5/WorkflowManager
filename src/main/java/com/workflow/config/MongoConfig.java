package com.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
@ComponentScan(basePackages="com.workflow")
public class MongoConfig {

    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
    	MongoClientURI uri = new MongoClientURI(
    		    "mongodb+srv://workflow:workflow@workflow-omher.mongodb.net/test?retryWrites=true");
        MongoClient mongoClient = new MongoClient(uri);
        return new SimpleMongoDbFactory(mongoClient, "workflow");
    }
 
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate;
    }
}