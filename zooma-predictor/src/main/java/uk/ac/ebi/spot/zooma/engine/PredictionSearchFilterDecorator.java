package uk.ac.ebi.spot.zooma.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
@Component("filter")
public class PredictionSearchFilterDecorator implements PredictionSearch{

    private PredictionSearch predictionSearch;

    @Autowired
    public PredictionSearchFilterDecorator(@Qualifier("primary.scorer") PredictionSearch predictionSearch) {
        this.predictionSearch = predictionSearch;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        return predictionSearch.search(propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyValuePattern, origin, originType, exclusiveOrigins);
        if (!exclusiveOrigins){
            return boostOrigin(predictions, origin, originType);
        }
        return predictions;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        return predictionSearch.search(propertyType, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyType, propertyValuePattern, origin, originType, exclusiveOrigins);
        if (!exclusiveOrigins){
            return boostOrigin(predictions, origin, originType);
        }
        return predictions;
    }

    private List<AnnotationPrediction> boostOrigin(List<AnnotationPrediction> predictions, List<String> origin, String originType){
        if(originType.equals("sources")){
            return boostSources(predictions, origin);
        } else if (originType.equals("topics")){
            return boostTopics(predictions, origin);
        }
        return predictions;
    }

    private List<AnnotationPrediction> boostSources(List<AnnotationPrediction> predictions, List<String> sources){
        predictions.stream().forEach(annotationPrediction -> {
            annotationPrediction.getSource().stream().forEach(s -> {
                if (sources.contains(s)) {
                    float score = annotationPrediction.getScore();
                    annotationPrediction.setScore(score * 1.5f); //score + 50% of score
                }
            });
        });
        return predictions;
    }

    private List<AnnotationPrediction> boostTopics(List<AnnotationPrediction> predictions, List<String> topics){
        predictions.stream().forEach(annotationPrediction -> {
            annotationPrediction.getTopic().stream().forEach(s -> {
                if(topics.contains(s)) {
                    float score = annotationPrediction.getScore();
                    annotationPrediction.setScore(score * 1.5f); //score + 50% of score
                }
            });
        });
        return predictions;
    }



}
