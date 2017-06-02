package uk.ac.ebi.spot.zooma.utils.predictor;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 01/06/2017.
 */
public class PredictorUtilsTest {

    @Test
    public void filterAnnotationPredictions() throws Exception {
        Prediction prediction1 = new AnnotationPrediction();
        prediction1.setScore(90);

        Prediction prediction2 = new AnnotationPrediction();
        prediction2.setScore(89);

        Prediction prediction3 = new AnnotationPrediction();
        prediction3.setScore(88);

        Prediction prediction4 = new AnnotationPrediction();
        prediction4.setScore(87);

        List<Prediction> list = new ArrayList<>();
        list.add(prediction1);
        list.add(prediction2);
        list.add(prediction3);
        list.add(prediction4);

        List<Prediction> predictions = PredictorUtils.filterAnnotationPredictions(list, 90, 0);
        assertTrue(predictions.size() == 1);

        predictions = PredictorUtils.filterAnnotationPredictions(list, 91, 0);
        assertTrue(predictions.size() == 0);

        predictions = PredictorUtils.filterAnnotationPredictions(list, 90, 0.8f);
        assertTrue(predictions.size() == 1);

        predictions = PredictorUtils.filterAnnotationPredictions(list, 0.8f);
        assertTrue(predictions.size() == 4);

        predictions = PredictorUtils.filterAnnotationPredictions(list, 1f);
        assertTrue(predictions.size() == 0);

        predictions = PredictorUtils.filterAnnotationPredictions(list, 0.99f);
        assertTrue(predictions.size() == 1);

        AnnotationPrediction prediction5 = new AnnotationPrediction();
        prediction5.setScore(70);
        list.add(prediction5);
        predictions = PredictorUtils.filterAnnotationPredictions(list, 0.8f);
        assertTrue(predictions.size() == 4);

        list = new ArrayList<>();
        predictions = PredictorUtils.filterAnnotationPredictions(list, 0.8f);
        assertTrue(predictions.size() == 0);

        list = null;
        predictions = PredictorUtils.filterAnnotationPredictions(list, 0.8f);
        assertTrue(predictions.size() == 0);
    }


    @Test
    public void shouldSearch() throws Exception {
        List<String> origins = null;
        assertTrue(PredictorUtils.shouldSearch(origins));

        origins = new ArrayList<>();
        assertTrue(PredictorUtils.shouldSearch(origins));

        origins.add("random");
        assertTrue(PredictorUtils.shouldSearch(origins));

        origins.add("none");
        assertFalse(PredictorUtils.shouldSearch(origins));

    }

    @Test
    public void normalizeToOneHundred() throws Exception {
        float score = PredictorUtils.normalizeToOneHundred(0, 0);
        assertTrue(score == 50);

        score = PredictorUtils.normalizeToOneHundred(1, 0);
        assertTrue(score == 50);

        score = PredictorUtils.normalizeToOneHundred(1, 1);
        assertTrue(score == 100);

        score = PredictorUtils.normalizeToOneHundred(-1, -1);
        assertTrue(score == 100);

        score = PredictorUtils.normalizeToOneHundred(10, 9);
        assertTrue(score < 100);

        score = PredictorUtils.normalizeToOneHundred(100, 0);
        assertTrue(score >= 0 && score <= 100);

    }

    @Test(expected = IllegalArgumentException.class)
    public void normalizeToOneHundredException() throws Exception {
        PredictorUtils.normalizeToOneHundred(1, 2);
    }



    @Test
    public void degradeToMaxScore() throws Exception {
        float score = PredictorUtils.degradeToMaxScore(0, 0, 0);
        assertTrue(score == 0);

        score = PredictorUtils.degradeToMaxScore(25, 100, 95);
        assertTrue(score > 0 && score <= 25);

        score = PredictorUtils.degradeToMaxScore(25, 100, 100);
        assertTrue(score == 25);

        score = PredictorUtils.degradeToMaxScore(25, 100, 0);
        assertTrue(score >= 0 && score <= 25);

    }

    @Test(expected = IllegalArgumentException.class)
    public void degradeToMaxScoreException() throws Exception {
        PredictorUtils.degradeToMaxScore(75, 0, 10);
    }


}