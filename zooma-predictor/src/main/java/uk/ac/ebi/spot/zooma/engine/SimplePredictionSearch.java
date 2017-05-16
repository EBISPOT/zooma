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
public class SimplePredictionSearch implements PredictionSearch {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String zoomaHttpLocation;

    @Autowired
    public SimplePredictionSearch(RestTemplate restTemplate,
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
                .path("/annotations/search")
                .queryParam("q", propertyValuePattern)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search")
                .queryParam("q", propertyValuePattern);

        if(exclusiveOrigins){
            filterOrigin(uriComponentsBuilder, origin, originType);
        }

        URI uri = uriComponentsBuilder.build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }


    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search")
                .queryParam("type", propertyType)
                .queryParam("q", propertyValuePattern)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search")
                .queryParam("type", propertyType)
                .queryParam("q", propertyValuePattern);

        if(exclusiveOrigins){
            filterOrigin(uriComponentsBuilder, origin, originType);
        }

        URI uri = uriComponentsBuilder.build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        return objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }


    private void filterOrigin(UriComponentsBuilder uriComponentsBuilder, List<String> origin, String originType) {
        if(!(originType.equals("sources") || originType.equals("topics"))){
            throw new IllegalArgumentException("Origin type must be either \"sources\" or \"topics\"!");
        }
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : origin){
            stringJoiner.add(s);
        }
        uriComponentsBuilder.queryParam(originType, stringJoiner.toString());
    }

}
