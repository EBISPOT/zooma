package uk.ac.ebi.spot.zooma.config;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Configuration
@EnableNeo4jRepositories("uk.ac.ebi.spot.zooma.repository.neo4j")
@EnableTransactionManagement
public class Neo4jConfig{

    @Bean
    public SessionFactory sessionFactory() {
        // with domain entity base package(s)
        SessionFactory sessionFactory = new SessionFactory("uk.ac.ebi.spot.zooma.model.neo4j");
        return sessionFactory;
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }

    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
