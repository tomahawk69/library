package org.library.parser.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ParsedFileTemplate extends MongoTemplate {

    @Autowired
    public ParsedFileTemplate(MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
        System.out.println(mongoDbFactory);
    }
}
