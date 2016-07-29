package org.library.parser;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {
    private String mongoHost;
    private Integer mongoPort;
    private String mongoDatabase;

    @Autowired
    public void setMongoHost(@Value("${mongo.host}") String mongoHost) {
        this.mongoHost = mongoHost;
    }

    @Autowired
    public void setMongoPort(@Value("${mongo.port}") Integer mongoPort) {
        this.mongoPort = mongoPort;
    }

    @Autowired
    public void setMongoDatabase(@Value("${mongo.database}") String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(mongoHost, mongoPort);
    }

    @Bean
    @Override
    public MongoDbFactory mongoDbFactory() throws Exception {
        return new SimpleMongoDbFactory(new MongoClient(mongoHost, mongoPort), mongoDatabase);
    }
}
