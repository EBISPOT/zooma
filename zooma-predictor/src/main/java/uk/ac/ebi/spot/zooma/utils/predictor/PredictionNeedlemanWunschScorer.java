package uk.ac.ebi.spot.zooma.utils.predictor;


import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

/**
 * Scores annotation summaries based on their quality and a lexical match measure (using the Needleman-Wunsch algorithm)
 * to derive the score.
 *
 * @author Tony Burdett
 * @date 18/12/13
 */
public class PredictionNeedlemanWunschScorer extends AbstractNeedlemanWunschScorer<Prediction> {
    /**
     * Extracts the annotated property value for the matched annotation prediction to compare to the search string
     *
     * @param matched the object that is being scored
     * @return the annotated property value for the given annotation prediction
     */
    @Override protected String extractMatchedString(Prediction matched) {
        return matched.getPropertyValue();
    }
}
