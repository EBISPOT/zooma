package uk.ac.ebi.spot.zooma.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.AnnotationSummaryNeedlemanWunschScorer;
import uk.ac.ebi.spot.zooma.utils.predictor.Scorer;

import java.util.Collections;

/**
 * Created by olgavrou on 20/04/2017.
 */
@Configuration
public class PredictorConfig {
    @Bean
    RestTemplate getRestTemplate(){

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(objectMapper());
        RestTemplate template = new RestTemplate(Collections.<HttpMessageConverter<?>> singletonList(converter));
        return template;
    }

    @Bean
    ObjectMapper objectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
        return mapper;
    }

    @Bean
    OLSClient olsClient(){
        String olsServer = null;//this.configuration.getProperty("ols.server");
        if (olsServer != null) {
            return new OLSClient(new OLSWsConfig(olsServer));
        } else {
            return new OLSClient(new OLSWsConfig());
        }
    }

    @Bean
    Scorer<AnnotationPrediction> scorer() {
        return new AnnotationSummaryNeedlemanWunschScorer();
    }

}
