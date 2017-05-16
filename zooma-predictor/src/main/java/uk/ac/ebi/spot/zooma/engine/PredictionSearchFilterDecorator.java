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
@Primary
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
            return boostOrigin(predictions, origin);
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
            return boostOrigin(predictions, origin);
        }
        return predictions;
    }


    private List<AnnotationPrediction> boostOrigin(List<AnnotationPrediction> predictions, List<String> origin){
        predictions.stream().forEach(annotationPrediction -> {
            annotationPrediction.getSource().stream().forEach(s -> {
                if(origin.contains(s)) {
                    float score = annotationPrediction.getScore();
                    annotationPrediction.setScore(score * 1.5f); //score + 50% of score
                }
            });
            annotationPrediction.getTopic().stream().forEach(s -> {
                if(origin.contains(s)) {
                    float score = annotationPrediction.getScore();
                    annotationPrediction.setScore(score * 1.5f); //score + 50% of score
                }
            });
        });
        //TODO add topic search if needed
        return predictions;
    }

}
