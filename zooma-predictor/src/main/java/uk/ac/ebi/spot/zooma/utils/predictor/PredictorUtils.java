package uk.ac.ebi.spot.zooma.utils.predictor;

import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.*;


/**
 * Some general ZOOMA utility functions
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class PredictorUtils {

    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     * <p/>
     * This form does not take a minimum score - this is equivalent to calling {@link
     * #filterAnnotationPredictions(List, float, float)} with a cutoff score of 0.
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffPercentage the maximum distance away from the top score an annotation is allowed to be whilst not
     *                         being filtered
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<Prediction> filterAnnotationPredictions(List<Prediction> summaries,
                                                                         float cutoffPercentage) {
        return filterAnnotationPredictions(summaries, 0, cutoffPercentage);

    }


    /**
     * Filter the supplied map of predictions to their score, reducing them down to a set of predictions that
     * exclude any unreasonable matches.  Predictions are excluded from the results with the following criteria: <ol>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     *
     * @param predictions        the set of summaries to filter
     * @param cutoffScore      the minimum allowed score for an annotation to not be filtered
     * @param cutoffPercentage the maximum
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<Prediction> filterAnnotationPredictions(final List<Prediction> predictions,
                                                                         float cutoffScore,
                                                                         float cutoffPercentage) {
        if(predictions == null || predictions.isEmpty()){
            return new ArrayList<>();
        }

        // return top scored summary
        List<Prediction> results = new ArrayList<>();

        Optional<Prediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float topScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;
        for (Prediction as : predictions) {
            float score = as.getScore();
            // if the score for this summary is within 5% of the top score,
            // AND if it is greater than the cutoff score
            // include
            if (score > (topScore * cutoffPercentage) && score >= cutoffScore) {
                results.add(as);
            }
        }

        results.sort(Comparator.comparing(Prediction::getScore));

        return results;
    }


    /*
     * Indicates whether a set of sources contains the None Selected checkbox, or if they are empty. If so then these sources
     * should not be searched.
     */
    public static  boolean shouldSearch(List<String> sources) {
        if(sources == null){
            return true;
        }
        for (String source : sources) {
            if (source.equals("none")) {
                return false;
            }
        }
        return true;
    }


    /**
     * Will normalize the score to 100 based on the delta percentage it has with the max score
     *
     * @param maxScore represents the max value a score can reach
     * @param score the score we want to normalise
     * @return the score will be 100 if it reached maxScore or
     * below 100 by the delta percentage it has with the maxScore
     */
    public static float normalizeToOneHundred(float maxScore, float score) {
        if (score > maxScore){
            throw new IllegalArgumentException("Score can not be higher than max score!");
        }

        if(maxScore == 0){
            return 50;
        }

        float dx = 100 * ((maxScore - score) / maxScore);
        float norm = 50 + (50 * (100 - dx) / 100);
        return norm;
    }

    /**
     * Degrades a score to the maxPossibleScore based on it's difference from the maxScore
     * @param maxPossibleScore the max possible score
     * @param maxScore the max of the scores before the degration
     * @param score the score to be degraded
     * @return
     */
    public static float degradeToMaxScore(float maxPossibleScore, float maxScore, float score){
        if (score > maxScore){
            throw new IllegalArgumentException("Score can not be higher than max score!");
        }

        float degrade = maxPossibleScore - (maxScore - score);
        if(degrade < 0){
            return 0;
        }
        return degrade;
    }

}
