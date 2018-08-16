package uk.ac.ebi.spot.zooma.scorers;

import lombok.Data;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;

import java.util.*;

/**
 * Created by olgavrou on 30/05/2017.
 */
@Data
public class ConfidenceCalculatorImpl extends AbstractConfidenceCalculator {

    private float cutoffScore;
    private float cutoffPercentage;
    private float maxOLSScore;
    private SimilarityScorer similarityScorer;

    public ConfidenceCalculatorImpl(float cutoffScore,
                                    float cutoffPercentage,
                                    float maxOLSScore,
                                    SimilarityScorer similarityScorer) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.maxOLSScore = maxOLSScore;
        this.similarityScorer = similarityScorer;
    }

    /**
     * Last scoring for confidence calculation logic.
     *
     * Predictions go through similarity scorer, get normalised to 100, get degraded to the max OLS score if they are OLS predictions
     * and get their confidence assigned to them
     *
     * @param predictions list of predictions
     * @param propertyValue the propertyValue we are predicting for
     * @return predictions with final score and confidence
     */
    @Override
    public List<Prediction> calculateFinalScore(List<Prediction> predictions, String propertyValue) {

        Optional<Prediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxScoreBeforeSimilarityScorer = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        List<Prediction> scoredResults = similarityScorer.score(predictions, propertyValue);

        //normalize to 100
        scoredResults.stream().forEach(annotation -> annotation.setScore(PredictorUtils.normalizeToOneHundred(maxScoreBeforeSimilarityScorer, annotation.getScore())));

        //cutoff 80%
        List<Prediction> results = PredictorUtils.filterAnnotationPredictions(predictions, cutoffPercentage);

//        maxAnn = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
//        float max = maxAnn.isPresent() ? maxAnn.get().getScore() : 100;
//        //normalize ols scores
//        results.stream().forEach(annotation -> {
//            if(((AnnotationPrediction)annotation).getType().equals(AnnotationPrediction.Type.OLS)) {
//                annotation.setScore(PredictorUtils.degradeToMaxScore(this.maxOLSScore, max, annotation.getScore()));
//            }
//        });

        results.sort(Comparator.comparing(Prediction::getScore));

        return results;
    }

    @Override
    public List<Prediction> setConfidence(List<Prediction> predictions){
        Prediction.Confidence confidence = getConfidence(predictions, cutoffScore);

        predictions.stream().forEach(annotationPrediction -> annotationPrediction.setConfidence(confidence));

        predictions.sort(Comparator.comparing(Prediction::getScore));

        return predictions;
    }



}
