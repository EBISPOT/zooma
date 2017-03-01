package uk.ac.ebi.spot.zooma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by olgavrou on 28/02/2017.
 */
@Configuration
@EnableMongoRepositories(basePackages = "uk.ac.ebi.spot.zooma.repository.mongo")
public class MongoConfig {
}
