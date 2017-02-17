//package uk.ac.ebi.spot.zooma.service;
//
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.apache.commons.lang3.tuple.Pair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import uk.ac.ebi.spot.zooma.model.*;
//import uk.ac.ebi.spot.zooma.model.api.Property;
//import uk.ac.ebi.spot.zooma.model.TypedProperty;
//import uk.ac.ebi.spot.zooma.utils.ZoomaUtils;
//
//import java.util.*;
//
///**
// * Created by olgavrou on 31/10/2016.
// */
//@Service
//public class AnnotationPredictionService {
//
//    @Autowired
//    private SolrSelectAnnotationsService solrSelectAnnotationsService;
//
//    @Autowired
//    private AnnotationSelector annotationSelector;
//
//    @Autowired
//    private TaskExecutor taskExecutor;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Value("${cutoff.percentage}")
//    private float cutoffPercentage;
//
//    @Value("${cutoff.score}")
//    private float cutoffScore;
//
//    public float getCutoffPercentage() {
//        return cutoffPercentage;
//    }
//
//    public float getCutoffScore() {
//        return cutoffScore;
//    }
//
//    private final Logger log = LoggerFactory.getLogger(getClass());
//
//    protected Logger getLog() {
//        return log;
//    }
//
//    public Map<Pair<String, String>, List<SimpleAnnotationPrediction>> predict(List<Property> properties, List<String> sources){
//        Map<Pair<String, String>, List<SimpleAnnotationPrediction>> summaryMap = new HashMap<>();
//        Map<Pair<String, String>, Predictor> predictors = new HashMap<>();
//
//        for (Property property : properties){
//            String annotatedPropertyValue = property.getPropertyValue();
//            String annotatedPropertyType = null;
//            if (property instanceof TypedProperty){
//                annotatedPropertyType = ((TypedProperty) property).getPropertyType();
//            }
//            //set threads searching
//            Predictor predictor = new Predictor(annotatedPropertyType, annotatedPropertyValue, sources);
//            taskExecutor.execute(predictor);
//            Pair<String, String> typeValuePair = new ImmutablePair<>(annotatedPropertyType, annotatedPropertyValue);
//            predictors.put(typeValuePair, predictor);
//
//        }
//
//
//        for (Pair<String, String> value : predictors.keySet()){
//            List<SimpleAnnotationPrediction> summaries = new ArrayList<>();
//            for (;;) {
//                if(predictors.get(value).getSimpleAnnotationPredictions() != null){
//                    //when predictions are calculated start putting into map
//                    //won't be null, will be populated with an empty array if no predictions found
//                    break;
//                }
//            }
//            summaries.addAll(predictors.get(value).getSimpleAnnotationPredictions());
//            if (!summaries.isEmpty()) {
//                summaryMap.put(value, summaries);
//            }
//        }
//        return summaryMap;
//    }
//
//    private class Predictor implements Runnable {
//        private String annotatedPropertyType;
//        private String annotatedPropertyValue;
//        private List<String> sourceNames;
//
//        private List<SimpleAnnotationPrediction> simpleAnnotationPredictions;
//
//        public Predictor(String annotatedPropertyType, String annotatedPropertyValue, List<String> sourceNames) {
//            this.annotatedPropertyType = annotatedPropertyType;
//            this.annotatedPropertyValue = annotatedPropertyValue;
//            this.sourceNames = sourceNames;
//        }
//
//        public List<SimpleAnnotationPrediction> getSimpleAnnotationPredictions() {
//            return simpleAnnotationPredictions;
//        }
//
//        public void setSimpleAnnotationPredictions(List<SimpleAnnotationPrediction> simpleAnnotationPredictions) {
//            this.simpleAnnotationPredictions = simpleAnnotationPredictions;
//        }
//
//        @Override
//        public void run() {
//            getLog().info("**** Starting search for: " + annotatedPropertyValue + " ****");
//            ArrayList<SimpleAnnotationPrediction> summaries = new ArrayList<>();
//
//            Map<String, Float> annotationSummaries;
//            //Query
//            if (annotatedPropertyType == null) {
//                annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
//            } else {
//                if (sourceNames == null || sourceNames.isEmpty()) {
//                    annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue);
//                } else {
//                    annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValueAndPropertyType(annotatedPropertyType, annotatedPropertyValue, sourceNames);
//                }
//                //if not populated, search just by annotatedPropertyValue
//                if (annotationSummaries == null || annotationSummaries.isEmpty()){
//                    annotationSummaries = queryByValue(annotatedPropertyValue, sourceNames);
//                }
//            }
//            //Score
//            Map<String, Float> goodAnnotationSummaries = annotationSelector.getGoodAnnotations(annotationSummaries, annotatedPropertyValue, getCutoffPercentage());
//
//            // now we have a list of annotation summaries; use this list to create predicted annotations
//            AnnotationPrediction.Confidence confidence = ZoomaUtils.getConfidence(goodAnnotationSummaries, getCutoffScore());
//
//            for (String id : goodAnnotationSummaries.keySet()){
////                ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/annotations/" + id, String.class);
//
//                Collection<String> st = new ArrayList<>();
//                st.add("semTag");
//                summaries.add(new SimpleAnnotationPrediction("propertyTYPE",
//                        "propertyValue",
//                        st,
//                        "source",
//                        goodAnnotationSummaries.get(id),
//                        confidence));
//            }
//
//            setSimpleAnnotationPredictions(summaries);
//            getLog().info("**** Search for: " + annotatedPropertyValue + " done! ****");
//
//        }
//
//        private Map<String, Float> queryByValue(String value, List<String> sourceNames){
//            Map<String, Float> annotationSummaries;
//            if (sourceNames == null || sourceNames.isEmpty()) {
//                annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValue(value);
//            } else {
//                annotationSummaries = solrSelectAnnotationsService.findAnnotationSummariesByPropertyValue(value, sourceNames);
//            }
//            return annotationSummaries;
//        }
//
//    }
//
//}
