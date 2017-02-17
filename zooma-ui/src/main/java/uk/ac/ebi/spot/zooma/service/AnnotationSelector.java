//package uk.ac.ebi.spot.zooma.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
//import uk.ac.ebi.spot.zooma.utils.Scorer;
//import uk.ac.ebi.spot.zooma.utils.SummaryUtils;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * Created by olgavrou on 04/11/2016.
// */
//@Service
//public class AnnotationSummarySelector {
//
//    @Autowired
//    Scorer<AnnotationSummary> scorer;
//
//    private final float MIN = minScore();
//
//    List<AnnotationSummary> getGoodAnnotationSummaries(List<AnnotationSummary> annotationSummaries, String annotatedPropertyValue, float cutoffPercentage){
//
//        float max = 1.0f;
//
//        //Get max and MIN qualities
//        for (AnnotationSummary annotationSummary : annotationSummaries){
//            if (annotationSummary.getQuality() > max){
//                max = annotationSummary.getQuality();
//            }
//        }
//
//        //Score
//        Map<AnnotationSummary, Float> annotationsToScore = scorer.score(annotationSummaries, annotatedPropertyValue);
//        Map<AnnotationSummary, Float> annotationsToNormScore = new HashMap<>();
//
//        //Normalise Score
//        for (AnnotationSummary annotation : annotationsToScore.keySet()){
//            float normScore = normaliseScore(annotationsToScore.get(annotation), max, MIN);
//            AnnotationSummary annotationSummary = (AnnotationSummary) annotation;
//            annotationSummary.setQuality(normScore);
//            annotationsToNormScore.put(annotationSummary, normScore);
//        }
//
//
//
//        //cutoff scores based on the difference between the first score
//        List<AnnotationSummary> results = SummaryUtils.filterAnnotationSummaries(annotationsToNormScore, cutoffPercentage);
//
//        //Make sure the results are sorted (highest score first).
//        Collections.sort(results, new Comparator<AnnotationSummary>() {
//            @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
//                return annotationsToNormScore.get(o2).compareTo(annotationsToNormScore.get(o1));
//            }
//        });
//
//        return results;
//    }
//
//    private float normaliseScore(float score, float max, float min){
//        if ((score - min) < 0) {
//            return 50;
//        }
//        else {
//            float n = 50 + (50 * (score - min)/(max - min));
//            return n;
//        }
//    }
//
//    private float minScore() {
//        // expected minimum score
//        Date y2k;
//        try {
//            //a really old date would make the quality not so good
//            y2k = new SimpleDateFormat("YYYY").parse("2000");
//        }
//        catch (ParseException e) {
//            throw new InstantiationError("Could not parse date '2000' (YYYY)");
//        }
//        float bottomQuality = (float) (1.0 + Math.log10(y2k.getTime()));
//        int sourceNumber = 1;
//        int numOfDocs = 1;
////        float normalizedFreq = 1.0f + (2 > 0 ? (numOfDocs / 2) : 0);
//        return  (bottomQuality + sourceNumber);
//    }
//}
