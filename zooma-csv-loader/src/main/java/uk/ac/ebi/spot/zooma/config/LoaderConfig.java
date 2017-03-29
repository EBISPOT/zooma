package uk.ac.ebi.spot.zooma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olgavrou on 08/02/2017.
 */
@Configuration
public class LoaderConfig{
    @Bean
    RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        return restTemplate;
    }
}
