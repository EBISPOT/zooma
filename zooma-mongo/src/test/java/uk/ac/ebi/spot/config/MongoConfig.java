package uk.ac.ebi.spot.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

/**
 * Created by olgavrou on 15/08/2016.
 */
@Configuration
@ComponentScan("uk.ac.ebi.spot")
public class MongoConfig {
}
