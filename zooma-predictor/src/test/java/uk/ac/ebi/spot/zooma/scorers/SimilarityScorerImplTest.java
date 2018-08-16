package uk.ac.ebi.spot.zooma.scorers;

import org.junit.Test;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictionNeedlemanWunschScorer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 02/06/2017.
 */
public class SimilarityScorerImplTest {

    @Test
    public void score() throws Exception {
        SimilarityScorerImpl scorer = new SimilarityScorerImpl(new PredictionNeedlemanWunschScorer());
        float initScore = 100;
        String initValue = "liver";

        AnnotationPrediction prediction = new AnnotationPrediction();
        prediction.setScore(initScore);
        prediction.setPropertyValue(initValue);

        List<Prediction> list = new ArrayList<>();
        list.add(prediction);

        List<Prediction> scores = scorer.score(list, initValue);
        assertTrue(scores.get(0).getScore() == initScore);

        prediction.setScore(initScore);
        scores = scorer.score(list, initValue + "1");
        assertTrue(scores.get(0).getScore() < initScore);

        prediction.setScore(initScore);
        scores = scorer.score(list, initValue.toUpperCase());
        assertTrue(scores.get(0).getScore() == initScore);

        prediction.setScore(initScore);
        scores = scorer.score(list, "live");
        assertTrue(scores.get(0).getScore() < initScore);
    }

}