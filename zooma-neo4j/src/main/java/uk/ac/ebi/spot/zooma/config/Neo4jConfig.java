package uk.ac.ebi.spot.zooma.config;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Configuration
@EnableNeo4jRepositories("uk.ac.ebi.spot.zooma.repository")
@EnableTransactionManagement
@ComponentScan("uk.ac.ebi.spot.zooma")
public class Neo4jConfig{

    @Bean
    public SessionFactory sessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory("uk.ac.ebi.spot.zooma.model");
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory().openSession());
    }

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

}
