package uk.ac.ebi.spot.zooma.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by olgavrou on 15/05/2017.
 */
@Component("delegate")
public class SimpleAnnotationPredictionSearch implements AnnotationPredictionSearch {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String zoomaHttpLocation;

    @Autowired
    public SimpleAnnotationPredictionSearch(RestTemplate restTemplate,
                                            ObjectMapper objectMapper,
                                            @Value("${zooma.solr.location}")String zoomaHttpLocation) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.zoomaHttpLocation = zoomaHttpLocation;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search/findByPropertyValue")
                .queryParam("propertyValue", propertyValuePattern)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> sources) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : sources){
            stringJoiner.add(s);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search/findByPropertyValue")
                .queryParam("propertyValue", propertyValuePattern)
                .queryParam("filter", stringJoiner.toString())
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search/findByPropertyTypeAndValue")
                .queryParam("propertyType", propertyType)
                .queryParam("propertyValue", propertyValuePattern)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> sources) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : sources){
            stringJoiner.add(s);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search/findByPropertyTypeAndValue")
                .queryParam("propertyType", propertyType)
                .queryParam("propertyValue", propertyValuePattern)
                .queryParam("filter", stringJoiner.toString())
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

    }

    @Override
    public List<AnnotationPrediction> searchByPreferredSources(String propertyValuePattern, List<String> preferredSources, List<String> requiredSources) {
        return null;
    }

    @Override
    public List<AnnotationPrediction> searchByPreferredSources(String propertyType, String propertyValuePattern, List<String> preferredSources, List<String> requiredSources) {
        return null;
    }
}
