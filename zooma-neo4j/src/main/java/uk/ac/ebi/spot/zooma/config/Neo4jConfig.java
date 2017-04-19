package uk.ac.ebi.spot.zooma.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.autoindex.AutoIndexMode;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.messaging.neo4j.AnnotationSubmissionReceiver;
import uk.ac.ebi.spot.zooma.service.neo4j.AnnotationService;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Configuration
@EnableNeo4jRepositories("uk.ac.ebi.spot.zooma.repository.neo4j")
@EnableTransactionManagement
public class Neo4jConfig{

    @Value("${spring.rabbitmq.activate}")
    Boolean activateRabbit;

    @Value("${spring.data.neo4j.uri}")
    String httpDriverUri;

    @Value("${spring.data.neo4j.username}")
    String username;

    @Value("${spring.data.neo4j.indexes.auto}")
    String assertMode;

    @Value("${spring.data.neo4j.password}")
    String password;

    @Autowired
    AnnotationService annotationService;

    @Autowired
    ObjectMapper objectMapper;


    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    org.neo4j.ogm.config.Configuration configuration(){
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration();
        configuration.driverConfiguration().setDriverClassName(HttpDriver.class.getName())
                .setURI(httpDriverUri);

        if(password != null || !password.isEmpty()){
            configuration.driverConfiguration().setCredentials(username, password);
        }

        configuration.autoIndexConfiguration().setAutoIndex(AutoIndexMode.fromString(assertMode).getName());

        return configuration;
    }


    @Bean
    AnnotationSubmissionReceiver receiver(AnnotationService service, ObjectMapper mapper){
        if(activateRabbit) {
            return new AnnotationSubmissionReceiver(service, mapper);
        } else {
            return null;
        }
    }

}
