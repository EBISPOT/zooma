package uk.ac.ebi.spot.zooma.scorers;

import lombok.Data;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.utils.predictor.Scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by olgavrou on 02/06/2017.
 */
@Data
public class SimilarityScorerImpl implements SimilarityScorer {

    private Scorer<Prediction> scorer;

    public SimilarityScorerImpl(Scorer<Prediction> scorer) {
        this.scorer = scorer;
    }

    public List<Prediction> score(List<Prediction> predictions, String similarityValue){

        //scorer based on text similarity
        Map<Prediction, Float> annotationsToScore = scorer.score(predictions, similarityValue);

        List<Prediction> scoredResults = new ArrayList<>();
        for(Prediction prediction : annotationsToScore.keySet()){
            prediction.setScore(annotationsToScore.get(prediction));
            scoredResults.add(prediction);
        }

        return scoredResults;
    }

}
