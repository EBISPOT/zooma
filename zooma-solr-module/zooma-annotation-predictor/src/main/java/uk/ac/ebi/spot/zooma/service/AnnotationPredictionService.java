package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.AnnotationPredictionRepository;
import uk.ac.ebi.spot.zooma.utils.PredictorUtils;

import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    @Autowired
    AnnotationPredictionRepository annotationPredictionRepository;

    @Value("${cutoff.score}")
    private float cutoffScore;

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

        List<AnnotationSummary> annotationSummaries = annotationPredictionRepository.getScoredAnnotationSummaries(annotatedPropertyType, annotatedPropertyValue, sourceNames);

        // now we have a list of annotation summaries; use this list to create predicted annotations
        AnnotationPrediction.Confidence confidence = PredictorUtils.getConfidence(annotationSummaries, getCutoffScore());

        for (AnnotationSummary annotation : annotationSummaries){
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



}
