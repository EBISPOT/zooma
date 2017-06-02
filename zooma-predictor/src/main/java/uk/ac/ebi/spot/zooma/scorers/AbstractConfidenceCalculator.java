package uk.ac.ebi.spot.zooma.scorers;

import uk.ac.ebi.spot.zooma.model.predictor.Confident;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.List;


/**
 * Created by olgavrou on 02/06/2017.
 */
public abstract class AbstractConfidenceCalculator {

    /**
     * Last scoring for confidence calculation logic.
     *
     * @param predictions list of predictions
     * @param propertyValue the propertyValue we are predicting for
     * @return predictions with final score and confidence
     */
    public abstract List<Prediction> calculateFinalScore(List<Prediction> predictions, String propertyValue);

    public abstract List<Prediction> setConfidence(List<Prediction> predictions);

    Confident.Confidence getConfidence(List<Prediction> results, float cutoffScore) {

        boolean achievedScore = false;
        for (Prediction t : results) {
            if (!achievedScore && t.getScore() > cutoffScore) {
                achievedScore = true;
                break; //won't come in here again
            }
        }

        if (results.spliterator().getExactSizeIfKnown() == 1 && achievedScore) {
            // one good annotation, so create prediction with high confidence
            return Confident.Confidence.HIGH;
        }
        else {
            if (achievedScore) {
                // multiple annotations each with a good score, create predictions with good confidence
                return Confident.Confidence.GOOD;
            }
            else {
                if (results.spliterator().getExactSizeIfKnown() == 1) {
                    // single stand out annotation that didn't achieve score, create prediction with good confidence
                    return Confident.Confidence.GOOD;
                }
                else {
                    // multiple annotations, none reached score, so create prediction with medium confidence
                    return Confident.Confidence.MEDIUM;
                }
            }
        }
    }
}
