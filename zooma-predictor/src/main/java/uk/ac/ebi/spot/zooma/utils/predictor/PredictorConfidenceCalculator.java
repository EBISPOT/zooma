package uk.ac.ebi.spot.zooma.utils.predictor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.*;

/**
 * Created by olgavrou on 30/05/2017.
 */
@Component
public class PredictorConfidenceCalculator {

    private Scorer<AnnotationPrediction> scorer;

    @Autowired
    public PredictorConfidenceCalculator(Scorer<AnnotationPrediction> scorer) {
        this.scorer = scorer;
    }

    public List<AnnotationPrediction> calculateConfidence(List<AnnotationPrediction> predictions, String propertyValue, float cutoffPercentage, float cutoffScore) {

        List<AnnotationPrediction> scoredResults = similarityScorer(predictions, propertyValue);
        //cutoff 80%
        List<AnnotationPrediction> results = cutOffAnnotations(scoredResults, cutoffPercentage);

        Optional<AnnotationPrediction> maxAnn = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float max = maxAnn.isPresent() ? maxAnn.get().getScore() : 100;
        //normalize ols scores
        results.stream().forEach(annotation -> {
            if(annotation.getStrongestMongoid().equals("ols")) {
                annotation.setScore(normalizeOLSScore(max, 75.0f, annotation.getScore()));
            }
        });

//         now we have a list of annotation summaries; use this list to create predicted annotations
        AnnotationPrediction.Confidence confidence = getConfidence(results, cutoffScore);

        results.stream().forEach(annotationPrediction -> annotationPrediction.setConfidence(confidence));

        return results;
    }

    private List<AnnotationPrediction> similarityScorer(List<AnnotationPrediction> predictions, String propertyValue){
        Optional<AnnotationPrediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxScoreBeforeSimilarityScorer = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        //scorer based on text similarity
        Map<AnnotationPrediction, Float> annotationsToScore = scorer.score(predictions, propertyValue);

        List<AnnotationPrediction> scoredResults = new ArrayList<>();
        scoredResults.addAll(annotationsToScore.keySet());

        //normalize to 100
        scoredResults.stream().forEach(annotation -> annotation.setScore(normScore(maxScoreBeforeSimilarityScorer, annotationsToScore.get(annotation))));
        return scoredResults;
    }

    private List<AnnotationPrediction> cutOffAnnotations(List<AnnotationPrediction> annotationSummaries, float cutoffPercentage){

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

    private AnnotationPrediction.Confidence getConfidence(List<AnnotationPrediction> results, float cutoffScore) {

        boolean achievedScore = false;
        for (AnnotationPrediction summary : results) {
            if (!achievedScore && summary.getScore() > cutoffScore) {
                achievedScore = true;
                break; //won't come in here again
            }
        }

        if (results.size() == 1 && achievedScore) {
            // one good annotation, so create prediction with high confidence
            return AnnotationPrediction.Confidence.HIGH;
        }
        else {
            if (achievedScore) {
                // multiple annotations each with a good score, create predictions with good confidence
                return AnnotationPrediction.Confidence.GOOD;
            }
            else {
                if (results.size() == 1) {
                    // single stand out annotation that didn't achieve score, create prediction with good confidence
                    return AnnotationPrediction.Confidence.GOOD;
                }
                else {
                    // multiple annotations, none reached score, so create prediction with medium confidence
                    return AnnotationPrediction.Confidence.MEDIUM;
                }
            }
        }
    }

    // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
    private float normScore(float maxScoreBeforeScorer, float scoreAfterScorer) {
        float dx = 100 * ((maxScoreBeforeScorer - scoreAfterScorer) / maxScoreBeforeScorer);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

    private float normalizeOLSScore(float topPredictionScore, float topNormalizedScore, float score){
        float normalize = topNormalizedScore - (topPredictionScore - score);
        return normalize;
    }

}
