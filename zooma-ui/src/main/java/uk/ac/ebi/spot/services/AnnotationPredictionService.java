package uk.ac.ebi.spot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.AnnotationPrediction;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SimpleAnnotationPrediction;
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
    AnnotationSummarySelector annotationSummarySelector;

    @Autowired
    TaskExecutor taskExecutor;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Map<String, List<SimpleAnnotationPrediction>> predict(ArrayList<String> annotatedPropertyValues){
        Map<String, List<SimpleAnnotationPrediction>> summaryMap = new HashMap<>();
        Map<String, Predictor> predictors = new HashMap<>();

        //set threads searching
        for (String value : annotatedPropertyValues){
            Predictor predictor = new Predictor(value);
            taskExecutor.execute(predictor);
            predictors.put(value, predictor);
        }

        for (String value : annotatedPropertyValues){
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
        private String annotatedPropertyValue;
        private List<SimpleAnnotationPrediction> simpleAnnotationPredictions;

        public Predictor(String annotatedPropertyValue) {
            this.annotatedPropertyValue = annotatedPropertyValue;
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

            //Query
            List<AnnotationSummary> annotationSummaries = solrAnnotationRepositoryService.getAnnotationSummariesByPropertyValue(annotatedPropertyValue);
            //Score
            List<AnnotationSummary> goodAnnotationSummaries = annotationSummarySelector.getGoodAnnotationSummaries(annotationSummaries, annotatedPropertyValue);

            // now we have a list of annotation summaries; use this list to create predicted annotations
            AnnotationPrediction.Confidence confidence = ZoomaUtils.getConfidence(goodAnnotationSummaries, 80f);

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

    }

}
