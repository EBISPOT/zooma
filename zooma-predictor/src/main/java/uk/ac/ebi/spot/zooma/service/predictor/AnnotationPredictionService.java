package uk.ac.ebi.spot.zooma.service.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;
import uk.ac.ebi.spot.zooma.utils.predictor.Scorer;


import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    private float cutoffScore;
    private float cutoffPercentage;
    private Scorer<AnnotationPrediction> scorer;
    PredictionSearch predictionSearch;

    @Autowired
    public AnnotationPredictionService(@Value("${cutoff.score}") float cutoffScore,
                                       @Value("${cutoff.percentage}") float cutoffPercentage,
                                       Scorer<AnnotationPrediction> scorer,
                                       PredictionSearch predictionSearch) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.scorer = scorer;
        this.predictionSearch = predictionSearch;
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
        List<AnnotationPrediction> results = predictionSearch.search(propertyValue);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueBoostSources(String propertyValue, List<String> sources) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyValue, sources, "sources", false);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueFilterSources(String propertyValue, List<String> sources) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyValue, sources, "sources", true);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueBoostTopics(String propertyValue, List<String> topics) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyValue, topics, "topics", false);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueFilterTopics(String propertyValue, List<String> topics) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyValue, topics, "topics", true);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValue(String propertyType, String propertyValue) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyType, propertyValue);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueBoostSources(String propertyType, String propertyValue, List<String> sources) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyType, propertyValue, sources, "sources", false);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueFilterSources(String propertyType, String propertyValue, List<String> sources) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyType, propertyValue, sources, "sources", true);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueBoostTopics(String propertyType, String propertyValue, List<String> topics) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyType, propertyValue, topics, "topics", false);
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueFilterTopics(String propertyType, String propertyValue, List<String> topics) throws URISyntaxException {
        List<AnnotationPrediction> results = predictionSearch.search(propertyType, propertyValue, topics, "topics", true);
        return calculateConfidence(results, propertyValue);
    }



    private List<AnnotationPrediction> calculateConfidence(List<AnnotationPrediction> results, String propertyValue) {

        Optional<AnnotationPrediction> maxAnn = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxNormalizedScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        //scorer based on text similarity
        Map<AnnotationPrediction, Float> annotationsToScore = scorer.score(results, propertyValue);

        List<AnnotationPrediction> scoredResults = new ArrayList<>();
        scoredResults.addAll(annotationsToScore.keySet());

        //normalize to 100
        scoredResults.stream().forEach(annotation -> annotation.setScore(normScore(maxNormalizedScore, annotationsToScore.get(annotation))));

        //cutoff 80%
        List<AnnotationPrediction> summaries = getGoodAnnotationSummaries(scoredResults, getCutoffPercentage());

//         now we have a list of annotation summaries; use this list to create predicted annotations
        AnnotationPrediction.Confidence confidence = PredictorUtils.getConfidence(summaries, getCutoffScore());

        summaries.stream().forEach(annotationPrediction -> annotationPrediction.setConfidence(confidence));

        getLog().info("**** Search for: {} done! ****", propertyValue);

        return summaries;
    }

    // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
    private float normScore(Float maxScoreBeforeScorer, Float scoreAfterScorer) {
        float dx = 100 * ((maxScoreBeforeScorer - scoreAfterScorer) / maxScoreBeforeScorer);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

    private List<AnnotationPrediction> getGoodAnnotationSummaries(List<AnnotationPrediction> annotationSummaries, float cutoffPercentage){

        Map<AnnotationPrediction, Float> annotationsToNormScore = new HashMap<>();
        for(AnnotationPrediction summary : annotationSummaries){
            annotationsToNormScore.put(summary, summary.getScore());
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
