package com.workflow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
@ComponentScan(basePackages="com.workflow")
@PropertySource("classpath:config.properties")
public class MongoConfig {

    @Autowired
    Environment env;

    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
    	MongoClientURI uri = new MongoClientURI(env.getProperty("MongoDbURI"));
        MongoClient mongoClient = new MongoClient(uri);
        return new SimpleMongoDbFactory(mongoClient, env.getProperty("DbName"));
    }
 
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate;
    }
}