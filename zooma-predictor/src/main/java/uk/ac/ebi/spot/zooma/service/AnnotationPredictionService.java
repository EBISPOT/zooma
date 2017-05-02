package uk.ac.ebi.spot.zooma.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.zooma.model.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.PredictorUtils;
import uk.ac.ebi.spot.zooma.utils.Scorer;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    private float cutoffScore;
    private float cutoffPercentage;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private Scorer<AnnotationPrediction> scorer;
    private String zoomaSolrLocation;

    @Autowired
    public AnnotationPredictionService(@Value("${cutoff.score}") float cutoffScore,
                                       @Value("${cutoff.percentage}") float cutoffPercentage,
                                       RestTemplate restTemplate,
                                       ObjectMapper objectMapper,
                                       Scorer<AnnotationPrediction> scorer,
                                       @Value("${zooma.solr.location}") String zoomaSolrLocation) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.scorer = scorer;
        this.zoomaSolrLocation = zoomaSolrLocation;
    }

    public float getCutoffScore() {
        return cutoffScore;
    }
    public float getCutoffPercentage() {
        return cutoffPercentage;
    }
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }


    public List<AnnotationPrediction> predictByPropertyValue(String propertyValue) throws URISyntaxException {
        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaSolrLocation)
                .path("/annotations/search/findByPropertyValue")
                .queryParam("propertyValue", propertyValue)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        List<AnnotationPrediction> results = objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueWithFilter(String propertyValue, List<String> sources) throws URISyntaxException {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : sources){
            stringJoiner.add(s);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaSolrLocation)
                .path("/annotations/search/findByPropertyValue")
                .queryParam("propertyValue", propertyValue)
                .queryParam("filter", stringJoiner.toString())
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        List<AnnotationPrediction> results = objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValue(String propertyType, String propertyValue) throws URISyntaxException {

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaSolrLocation)
                .path("/annotations/search/findByPropertyTypeAndValue")
                .queryParam("propertyType", propertyType)
                .queryParam("propertyValue", propertyValue)
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        List<AnnotationPrediction> results = objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueWithFilter(String propertyType, String propertyValue, List<String> sources) throws URISyntaxException {

        StringJoiner stringJoiner = new StringJoiner(",");
        for (String s : sources){
            stringJoiner.add(s);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(this.zoomaSolrLocation)
                .path("/annotations/search/findByPropertyTypeAndValue")
                .queryParam("propertyType", propertyType)
                .queryParam("propertyValue", propertyValue)
                .queryParam("filter", stringJoiner.toString())
                .build().toUri();

        PagedResources<AnnotationPrediction> r = restTemplate
                .getForObject(uri,
                        PagedResources.class);

        List<AnnotationPrediction> results = objectMapper.convertValue(r.getContent() , new TypeReference<List<AnnotationPrediction>>(){});

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    private List<AnnotationPrediction> calculateConfidence(List<AnnotationPrediction> results, String propertyValue) {
        Optional<AnnotationPrediction> maxAnn = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));

        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        results.stream().forEach(annotation -> annotation.setQuality(annotation.getScore()));

        Map<AnnotationPrediction, Float> annotationsToScore = scorer.score(results, propertyValue);

        List<AnnotationPrediction> scoredResults = new ArrayList<>();
        scoredResults.addAll(annotationsToScore.keySet());
        scoredResults.stream().forEach(annotation -> annotation.setQuality(normScore(maxSolrScore, annotationsToScore.get(annotation))));

        List<AnnotationPrediction> summaries = getGoodAnnotationSummaries(scoredResults, getCutoffPercentage());

//         now we have a list of annotation summaries; use this list to create predicted annotations
        AnnotationPrediction.Confidence confidence = PredictorUtils.getConfidence(summaries, getCutoffScore());

        summaries.stream().forEach(annotationPrediction -> annotationPrediction.setConfidence(confidence));

        getLog().info("**** Search for: {} done! ****", propertyValue);

        return summaries;
    }

    // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
    private float normScore(Float maxSolrScoreBeforeScorer, Float scoreAfterScorer) {
        float dx = 100 * ((maxSolrScoreBeforeScorer - scoreAfterScorer) / maxSolrScoreBeforeScorer);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

    private List<AnnotationPrediction> getGoodAnnotationSummaries(List<AnnotationPrediction> annotationSummaries, float cutoffPercentage){

        Map<AnnotationPrediction, Float> annotationsToNormScore = new HashMap<>();
        for(AnnotationPrediction summary : annotationSummaries){
            annotationsToNormScore.put(summary, summary.getQuality());
        }

        //cutoff scores based on the difference between the first score
        List<AnnotationPrediction> results = PredictorUtils.filterAnnotationSummaries(annotationsToNormScore, cutoffPercentage);

        //TODO: java 8
        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationPrediction>() {
            @Override public int compare(AnnotationPrediction o1, AnnotationPrediction o2) {
                return annotationsToNormScore.get(o2).compareTo(annotationsToNormScore.get(o1));
            }
        });

        return results;
    }

}
