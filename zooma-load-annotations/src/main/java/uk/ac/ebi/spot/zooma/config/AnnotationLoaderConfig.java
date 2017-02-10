package uk.ac.ebi.spot.zooma.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
/**
 * Created by olgavrou on 07/02/2017.
 */
@Configuration
public class AnnotationLoaderConfig {

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

}
