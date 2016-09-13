package uk.ac.ebi.spot.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

/**
 * Created by olgavrou on 13/09/2016.
 */
@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration {

    @Value("${mongo.db.name}")
    String mongodbName;

    @Value("${mongo.db.port}")
    String mongodbPort;


    @Override
    protected String getDatabaseName() {
        return mongodbName;
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient(mongodbPort);
    }


}

