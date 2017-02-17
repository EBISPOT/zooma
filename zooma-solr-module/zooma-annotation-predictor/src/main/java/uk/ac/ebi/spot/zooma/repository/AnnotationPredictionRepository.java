package uk.ac.ebi.spot.zooma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.service.AnnotationSummaryService;
import uk.ac.ebi.spot.zooma.utils.PredictorUtils;

import java.util.*;

/**
 * Created by olgavrou on 17/02/2017.
 */
@Repository
public class AnnotationPredictionRepository {

    @Value("${cutoff.percentage}")
    private float cutoffPercentage;

    @Autowired
    private AnnotationSummaryService annotationSummaryService;

    public float getCutoffPercentage() {
        return cutoffPercentage;
    }

    public  List<AnnotationSummary> getScoredAnnotationSummaries(String propertyType, String propertyValue, List<String> desiredSources){
        List<AnnotationSummary> annotationSummaries = getAnnotationSummaries(propertyType, propertyValue, desiredSources);
        annotationSummaries = getGoodAnnotationSummaries(annotationSummaries, getCutoffPercentage());
        return annotationSummaries;
    }


    private List<AnnotationSummary> getAnnotationSummaries(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames) {
        List<AnnotationSummary> annotationSummaries;
        //Query
        if (annotatedPropertyType == null) {
            annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
        } else {
            if (sourceNames == null || sourceNames.isEmpty()) {
                annotationSummaries = annotationSummaryService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue);
            } else {
                annotationSummaries = annotationSummaryService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue, sourceNames);
            }
            //if not populated, search just by annotatedPropertyValue
            if (annotationSummaries == null || annotationSummaries.isEmpty()){
                annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
            }
        }
        return annotationSummaries;
    }


    private List<AnnotationSummary> queryByValue(String value, List<String> sourceNames){
        List<AnnotationSummary> annotationSummaries;
        if (sourceNames == null || sourceNames.isEmpty()) {
            annotationSummaries = annotationSummaryService.findAnnotationSummariesByPropertyValue(value);
        } else {
            annotationSummaries = annotationSummaryService.findAnnotationSummariesByPropertyValue(value, sourceNames);
        }
        return annotationSummaries;
    }

    private List<AnnotationSummary> getGoodAnnotationSummaries(List<AnnotationSummary> annotationSummaries, float cutoffPercentage){

        Map<AnnotationSummary, Float> annotationsToNormScore = new HashMap<>();
        for(AnnotationSummary summary : annotationSummaries){
            annotationsToNormScore.put(summary, summary.getQuality());
        }

        //cutoff scores based on the difference between the first score
        List<AnnotationSummary> results = PredictorUtils.filterAnnotationSummaries(annotationsToNormScore, cutoffPercentage);

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationSummary>() {
            @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
                return annotationsToNormScore.get(o2).compareTo(annotationsToNormScore.get(o1));
            }
        });

        return results;
    }

}
