package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.utils.ZoomaUtils;

import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    @Autowired
    private SolrSelectAnnotationsService solrSelectAnnotationsService;

    @Autowired
    SolrTemplate solrTemplate;

    @Value("${cutoff.percentage}")
    private float cutoffPercentage;

    @Value("${cutoff.score}")
    private float cutoffScore;

    public float getCutoffPercentage() {
        return cutoffPercentage;
    }

    public float getCutoffScore() {
        return cutoffScore;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }


    public ArrayList<AnnotationPrediction> predict(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames){

        getLog().info("**** Starting search for: " + annotatedPropertyValue + " ****");
        ArrayList<AnnotationPrediction> summaries = new ArrayList<>();

        List<AnnotationSummary> annotationSummaries = getAnnotationSummaries(annotatedPropertyType, annotatedPropertyValue, sourceNames);
        //Score
        List<AnnotationSummary> goodAnnotationSummaries = getGoodAnnotationSummaries(annotationSummaries, annotatedPropertyValue, getCutoffPercentage());

        // now we have a list of annotation summaries; use this list to create predicted annotations
        AnnotationPrediction.Confidence confidence = ZoomaUtils.getConfidence(goodAnnotationSummaries, getCutoffScore());

        for (AnnotationSummary annotation : goodAnnotationSummaries){
//                ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/annotations/" + id, String.class);
            String propertyType;
            if(annotatedPropertyType == null){
                propertyType = annotation.getPropertyType();
            } else {
                propertyType = annotatedPropertyType;
            }
            summaries.add(new AnnotationPrediction(propertyType,
                    annotation.getPropertyValue(),
                    annotation.getSemanticTags(),
                    annotation.getSource(),
                    annotation.getQuality(),
                    confidence,
                    annotation.getMongoid()));
        }

        getLog().info("**** Search for: " + annotatedPropertyValue + " done! ****");

        return summaries;
    }

    private List<AnnotationSummary> getAnnotationSummaries(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames) {
        List<AnnotationSummary> annotationSummaries;
        //Query
        if (annotatedPropertyType == null) {
            annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
        } else {
            if (sourceNames == null || sourceNames.isEmpty()) {
                annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue);
            } else {
                annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue, sourceNames);
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
            annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValue(value);
        } else {
            annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValue(value, sourceNames);
        }
        return annotationSummaries;
    }

    private List<AnnotationSummary> getGoodAnnotationSummaries(List<AnnotationSummary> annotationSummaries, String annotatedPropertyValue, float cutoffPercentage){

        float max = 1.0f;

        //Get max and MIN qualities
        for (AnnotationSummary annotationSummary : annotationSummaries){
            if (annotationSummary.getQuality() > max){
                max = annotationSummary.getQuality();
            }
        }

        Map<AnnotationSummary, Float> annotationsToNormScore = new HashMap<>();

        //Normalise Score
        for (AnnotationSummary annotation : annotationSummaries){
//            float normScore = ZoomaUtils.normaliseScore(annotation.getQuality(), max);
//            AnnotationSummary annotationSummary = (AnnotationSummary) annotation;
//            annotationSummary.setQuality(normScore);
            annotationsToNormScore.put(annotation, annotation.getQuality());
        }



        //cutoff scores based on the difference between the first score
        List<AnnotationSummary> results = ZoomaUtils.filterAnnotationSummaries(annotationsToNormScore, cutoffPercentage);

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationSummary>() {
            @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
                return annotationsToNormScore.get(o2).compareTo(annotationsToNormScore.get(o1));
            }
        });

        return results;
    }

}
