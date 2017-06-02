package uk.ac.ebi.spot.zooma.scorers;

import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.List;

/**
 * Created by olgavrou on 02/06/2017.
 */
public interface SimilarityScorer {

    List<Prediction> score(List<Prediction> predictions, String similarityValue);

}
