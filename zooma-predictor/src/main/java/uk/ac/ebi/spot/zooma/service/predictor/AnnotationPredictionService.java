package uk.ac.ebi.spot.zooma.service.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.engine.AnnotationPredictionSearch;
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
    AnnotationPredictionSearch annotationPredictionSearch;

    @Autowired
    public AnnotationPredictionService(@Value("${cutoff.score}") float cutoffScore,
                                       @Value("${cutoff.percentage}") float cutoffPercentage,
                                       Scorer<AnnotationPrediction> scorer,
                                       AnnotationPredictionSearch annotationPredictionSearch) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.scorer = scorer;
        this.annotationPredictionSearch = annotationPredictionSearch;
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

        List<AnnotationPrediction> results = annotationPredictionSearch.search(propertyValue);

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyValueWithFilter(String propertyValue, List<String> sources) throws URISyntaxException {

        List<AnnotationPrediction> results = annotationPredictionSearch.search(propertyValue, sources);

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValue(String propertyType, String propertyValue) throws URISyntaxException {

        List<AnnotationPrediction> results = annotationPredictionSearch.search(propertyType, propertyValue);

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueWithFilter(String propertyType, String propertyValue, List<String> sources) throws URISyntaxException {

        List<AnnotationPrediction> results = annotationPredictionSearch.search(propertyType, propertyValue, sources);

        //scoring
        return calculateConfidence(results, propertyValue);
    }

    private List<AnnotationPrediction> calculateConfidence(List<AnnotationPrediction> results, String propertyValue) {

        int totalDocumentsFound = 0;
        for (AnnotationPrediction prediction : results){
            totalDocumentsFound = totalDocumentsFound + prediction.getVotes();
        }

        //effectively final
        int totDoc = totalDocumentsFound;
        Optional<AnnotationPrediction> maxAnn = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        results.stream().forEach(annotation -> {
            float sourceNumber = annotation.getSource().size();
            float numOfDocs = annotation.getVotes();
            float topQuality = annotation.getQuality();
            float normalizedFreq = 1.0f + (totDoc > 0 ? (numOfDocs / totDoc) : 0);
            float normalizedSolrScore = 1.0f + annotation.getScore() / maxSolrScore;
            float score = (topQuality + sourceNumber) * normalizedSolrScore * normalizedFreq;
            annotation.setScore(score);
        });

        //scorer based on text similarity
        Map<AnnotationPrediction, Float> annotationsToScore = scorer.score(results, propertyValue);

        List<AnnotationPrediction> scoredResults = new ArrayList<>();
        scoredResults.addAll(annotationsToScore.keySet());

        maxAnn = scoredResults.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxNormalizedScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

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
    private float normScore(Float maxSolrScoreBeforeScorer, Float scoreAfterScorer) {
        float dx = 100 * ((maxSolrScoreBeforeScorer - scoreAfterScorer) / maxSolrScoreBeforeScorer);
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
