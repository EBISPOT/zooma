package uk.ac.ebi.spot.zooma.scorers;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Confident;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictionNeedlemanWunschScorer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 05/06/2017.
 */
public class ConfidenceCalculatorImplTest {

    private List<Prediction> highConf = new ArrayList<>();
    private List<Prediction> goodAchivedScore = new ArrayList<>();
    private List<Prediction> goodScoreNotAchived = new ArrayList<>();
    private List<Prediction> mediumConfDiffPropValue = new ArrayList<>();
    private List<Prediction> mediumConfSamePropValue = new ArrayList<>();
    private float cutoffScore = 10;
    String value = "liver";
    private SimilarityScorer similarityScorer = new SimilarityScorerImpl(new PredictionNeedlemanWunschScorer());
    private ConfidenceCalculatorImpl calculator = new ConfidenceCalculatorImpl(cutoffScore, 0.9f, 75f, similarityScorer);

    @Before
    public void setup(){
        AnnotationPrediction prediction = new AnnotationPrediction();
        prediction.setScore(cutoffScore + 1);
        prediction.setPropertyValue(value);
        highConf.add(prediction);

        AnnotationPrediction prediction1 = new AnnotationPrediction();
        prediction1.setPropertyValue(value);
        prediction1.setScore(cutoffScore + 1);
        AnnotationPrediction prediction2 = new AnnotationPrediction();
        prediction2.setScore(cutoffScore + 1);
        prediction2.setPropertyValue(value);
        goodAchivedScore.add(prediction1);
        goodAchivedScore.add(prediction2);


        AnnotationPrediction prediction3 = new AnnotationPrediction();
        prediction3.setScore(cutoffScore);
        prediction3.setPropertyValue(value);
        goodScoreNotAchived.add(prediction3);

        AnnotationPrediction prediction4 = new AnnotationPrediction();
        prediction4.setScore(cutoffScore + 1);
        prediction4.setPropertyValue(value);
        AnnotationPrediction prediction5 = new AnnotationPrediction();
        prediction5.setScore(cutoffScore);
        prediction5.setPropertyValue(value + " 1");
        prediction5.setType(AnnotationPrediction.Type.OLS);
        mediumConfDiffPropValue.add(prediction4);
        mediumConfDiffPropValue.add(prediction5);

        AnnotationPrediction prediction6 = new AnnotationPrediction();
        prediction6.setScore(cutoffScore + 1);
        prediction6.setPropertyValue(value);
        AnnotationPrediction prediction7 = new AnnotationPrediction();
        prediction7.setScore(cutoffScore);
        prediction7.setPropertyValue(value);
        prediction7.setType(AnnotationPrediction.Type.OLS);
        mediumConfSamePropValue.add(prediction6);
        mediumConfSamePropValue.add(prediction7);

    }

    @Test
    public void calculateFinalScore() throws Exception {
        //similarity scorer, normalize to 100, filter with cutoff score and normalize OLS scores to 75
        List<Prediction> predictions = new ArrayList<>();
        predictions = calculator.calculateFinalScore(highConf, value);
        assertTrue(predictions.size() == highConf.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getScore() == 100);
        }

        predictions = calculator.calculateFinalScore(goodAchivedScore, value + " 1");
        assertTrue(predictions.size() == goodAchivedScore.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getScore() <= 100);
        }

        predictions = calculator.calculateFinalScore(mediumConfDiffPropValue, value);
        assertTrue(predictions.size() < mediumConfDiffPropValue.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getScore() <= 100);
        }

        predictions = calculator.calculateFinalScore(mediumConfSamePropValue, value);
        assertTrue(predictions.size() == mediumConfSamePropValue.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getScore() <= 100);
            if(((AnnotationPrediction)prediction).getType().equals(AnnotationPrediction.Type.OLS)){
                assertTrue(prediction.getScore() <= 75);
            }
        }
    }

    @Test
    public void setConfidence() throws Exception {
        //calcs conf, sets, and sorts
        List<Prediction> predictions = new ArrayList<>();
        predictions = calculator.setConfidence(highConf);
        assertTrue(predictions.size() == highConf.size());
        assertTrue(predictions.get(0).getConfidence().equals(Confident.Confidence.HIGH));

        predictions = calculator.setConfidence(goodAchivedScore);
        assertTrue(predictions.size() == goodAchivedScore.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getConfidence().equals(Confident.Confidence.GOOD));
        }

        predictions = calculator.setConfidence(goodScoreNotAchived);
        assertTrue(predictions.size() == goodScoreNotAchived.size());
        assertTrue(predictions.get(0).getConfidence().equals(Confident.Confidence.GOOD));

        predictions = calculator.setConfidence(mediumConfDiffPropValue);
        assertTrue(predictions.size() == mediumConfDiffPropValue.size());
        for(Prediction prediction : predictions){
            assertTrue(prediction.getConfidence().equals(Confident.Confidence.MEDIUM));
        }

        //assert medium is sorted
        float current = 0;
        for(Prediction prediction: predictions){
            assertTrue(prediction.getScore() >= current);
            current = prediction.getScore();
        }
    }

    @Test
    public void getConfidence() throws Exception {

        Confident.Confidence confidence = calculator.getConfidence(highConf, cutoffScore);
        assertTrue(confidence.equals(Confident.Confidence.HIGH));

        confidence = calculator.getConfidence(goodAchivedScore, cutoffScore);
        assertTrue(confidence.equals(Confident.Confidence.GOOD));

        confidence = calculator.getConfidence(goodScoreNotAchived, cutoffScore);
        assertTrue(confidence.equals(Confident.Confidence.GOOD));

        confidence = calculator.getConfidence(mediumConfDiffPropValue, cutoffScore);
        assertTrue(confidence.equals(Confident.Confidence.MEDIUM));
    }

}