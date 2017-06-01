package uk.ac.ebi.spot.zooma.engine.concrete.impl;

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
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.ParetoDistributionTransformation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Created by olgavrou on 15/05/2017.
 */
@Component("simple.delegate")
public class SimplePredictionSearch implements PredictionSearch {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String zoomaHttpLocation;
    private ParetoDistributionTransformation transformation;

    @Autowired
    public SimplePredictionSearch(RestTemplate restTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${zooma.solr.location}")String zoomaHttpLocation) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.zoomaHttpLocation = zoomaHttpLocation;
        this.transformation = new ParetoDistributionTransformation(2);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        return this.searchWithOrigin(propertyValuePattern, new ArrayList<>(), false);
    }

    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        getLog().debug("simple Solr search for {} ", propertyValuePattern);
        UriComponentsBuilder uriComponentsBuilder = constructQuery(propertyValuePattern);

        if(filter){
            filterOrigin(uriComponentsBuilder, origin);
            getLog().debug("simple Solr search for {} adding filter for origin {} ", propertyValuePattern, origin);
        }

        URI uri = uriComponentsBuilder.build().toUri();

        List<AnnotationPrediction> predictions = getForObject(uri);
        return primaryMetaScore(predictions);
    }


    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        return this.searchWithOrigin(propertyType, propertyValuePattern, new ArrayList<>(), false);
    }

    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> origin, boolean filter) {
        getLog().debug("simple Solr search for {} with type {} ", propertyValuePattern, propertyType);
        UriComponentsBuilder uriComponentsBuilder = constructQuery(propertyType, propertyValuePattern);
        if(filter){
            filterOrigin(uriComponentsBuilder, origin);
            getLog().debug("simple Solr search for {} with type {} adding filter for origin {} ", propertyValuePattern, propertyType, origin);
        }
        URI uri = uriComponentsBuilder.build().toUri();

        List<AnnotationPrediction> predictions = getForObject(uri);
        return primaryMetaScore(predictions);
    }


    private void filterOrigin(UriComponentsBuilder uriComponentsBuilder, List<String> origin) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : origin){
            stringJoiner.add(s);
        }
        uriComponentsBuilder.queryParam("origins", stringJoiner.toString());
    }

    private UriComponentsBuilder constructQuery(String propertyValue){
        return UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search")
                .queryParam("q", propertyValue);
    }

    private UriComponentsBuilder constructQuery(String propertyType, String propertyValue){
        return UriComponentsBuilder.fromHttpUrl(this.zoomaHttpLocation)
                .path("/annotations/search")
                .queryParam("type", propertyType)
                .queryParam("q", propertyValue);
    }

    private List<AnnotationPrediction> getForObject(URI uri){
        PagedResources<AnnotationPrediction> resources = restTemplate
                .getForObject(uri,
                        PagedResources.class);
        return objectMapper.convertValue(resources.getContent() , new TypeReference<List<AnnotationPrediction>>(){});
    }

    private List<AnnotationPrediction> primaryMetaScore(List<AnnotationPrediction> predictions){
        Optional<AnnotationPrediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        predictions.stream().forEach(annotation -> {
            float sourceNumber = annotation.getSource().size();
            float numOfDocs = annotation.getVotes();
            float topQuality = annotation.getQuality();
            float normalizedSolrScore = 1.0f + annotation.getScore() / maxSolrScore;
            float pareto = this.transformation.transform(numOfDocs);
            float score = (topQuality + sourceNumber + pareto) * normalizedSolrScore;
            annotation.setScore(score);
        });

        return predictions;
    }

}
