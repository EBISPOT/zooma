package uk.ac.ebi.spot.services;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.util.ZoomaUtils;

import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    @Autowired
    private SolrAnnotationRepositoryService solrAnnotationRepositoryService;

    @Autowired
    private AnnotationSummarySelector annotationSummarySelector;

    @Autowired
    private TaskExecutor taskExecutor;

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

    public Map<Pair<String, String>, List<SimpleAnnotationPrediction>> predict(List<Property> properties, List<String> sources){
        Map<Pair<String, String>, List<SimpleAnnotationPrediction>> summaryMap = new HashMap<>();
        Map<Pair<String, String>, Predictor> predictors = new HashMap<>();

        for (Property property : properties){
            String annotatedPropertyValue = property.getPropertyValue();
            String annotatedPropertyType = null;
            if (property instanceof TypedProperty){
                annotatedPropertyType = ((TypedProperty) property).getPropertyType();
            }
            //set threads searching
            Predictor predictor = new Predictor(annotatedPropertyType, annotatedPropertyValue, sources);
            taskExecutor.execute(predictor);
            Pair<String, String> typeValuePair = new ImmutablePair<>(annotatedPropertyType, annotatedPropertyValue);
            predictors.put(typeValuePair, predictor);

        }


        for (Pair<String, String> value : predictors.keySet()){
            List<SimpleAnnotationPrediction> summaries = new ArrayList<>();
            for (;;) {
                if(predictors.get(value).getSimpleAnnotationPredictions() != null){
                    //when predictions are calculated start putting into map
                    //won't be null, will be populated with an empty array if no predictions found
                    break;
                }
            }
            summaries.addAll(predictors.get(value).getSimpleAnnotationPredictions());
            if (!summaries.isEmpty()) {
                summaryMap.put(value, summaries);
            }
        }
        return summaryMap;
    }

    private class Predictor implements Runnable {
        private String annotatedPropertyType;
        private String annotatedPropertyValue;
        private List<String> sourceNames;

        private List<SimpleAnnotationPrediction> simpleAnnotationPredictions;

        public Predictor(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames) {
            this.annotatedPropertyType = annotatedPropertyType;
            this.annotatedPropertyValue = annotatedPropertyValue;
            this.sourceNames = sourceNames;
        }

        public List<SimpleAnnotationPrediction> getSimpleAnnotationPredictions() {
            return simpleAnnotationPredictions;
        }

        public void setSimpleAnnotationPredictions(List<SimpleAnnotationPrediction> simpleAnnotationPredictions) {
            this.simpleAnnotationPredictions = simpleAnnotationPredictions;
        }

        @Override
        public void run() {
            getLog().info("**** Starting search for: " + annotatedPropertyValue + " ****");
            List<SimpleAnnotationPrediction> summaries = new ArrayList<>();

            List<AnnotationSummary> annotationSummaries;
            //Query
            if (annotatedPropertyType == null) {
                annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
            } else {
                if (sourceNames == null || sourceNames.isEmpty()) {
                    annotationSummaries = solrAnnotationRepositoryService.getAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue);
                } else {
                    annotationSummaries = solrAnnotationRepositoryService.getAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue, sourceNames);
                }
                //if not populated, search just by annotatedPropertyValue
                if (annotationSummaries == null || annotationSummaries.isEmpty()){
                    annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
                }
            }
            //Score
            List<AnnotationSummary> goodAnnotationSummaries = annotationSummarySelector.getGoodAnnotationSummaries(annotationSummaries, annotatedPropertyValue, getCutoffPercentage());

            // now we have a list of annotation summaries; use this list to create predicted annotations
            AnnotationPrediction.Confidence confidence = ZoomaUtils.getConfidence(goodAnnotationSummaries, getCutoffScore());

            for (AnnotationSummary summary : goodAnnotationSummaries){
                summaries.add(new SimpleAnnotationPrediction(summary.getAnnotatedPropertyType(),
                        summary.getAnnotatedPropertyValue(),
                        summary.getSemanticTags(),
                        summary.getSource(),
                        summary.getQuality(),
                        confidence));
            }

            setSimpleAnnotationPredictions(summaries);
            getLog().info("**** Search for: " + annotatedPropertyValue + " done! ****");

        }

        private List<AnnotationSummary> queryByValue(String value, List<String> sourceNames){
            List<AnnotationSummary> annotationSummaries;
            if (sourceNames == null || sourceNames.isEmpty()) {
                annotationSummaries = solrAnnotationRepositoryService.getAnnotationSummariesByPropertyValue(value);
            } else {
                annotationSummaries = solrAnnotationRepositoryService.getAnnotationSummariesByPropertyValue(value, sourceNames);
            }
            return annotationSummaries;
        }

    }

}
