package uk.ac.ebi.spot.zooma.engine.decorators;

import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
public class PredictionSearchBoostOriginDecorator extends PredictionSearchDecorator{

    public PredictionSearchBoostOriginDecorator(PredictionSearch predictionSearch) {
        super(predictionSearch);

    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        return super.search(propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        List<AnnotationPrediction> predictions = super.searchWithOrigin(propertyValuePattern, origin, filter);
        if(!filter){
            return boostOrigin(predictions, origin);
        }
        return predictions;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        return super.search(propertyType, propertyValuePattern);
    }


    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> ontologies, boolean filter) {
        List<AnnotationPrediction> predictions = super.searchWithOrigin(propertyType, propertyValuePattern, ontologies, filter);
        if(!filter){
            return boostOrigin(predictions, ontologies);
        }
        return predictions;

    }


    private List<AnnotationPrediction> boostOrigin(List<AnnotationPrediction> predictions, List<String> origin){
        for (AnnotationPrediction prediction : predictions){
            Collection<String> origins = new ArrayList<>();
            origins.addAll(prediction.getSource());
            origins.addAll(prediction.getTopic());
            for(String o : origins){
                if (origin.contains(o)) {
                    float score = prediction.getScore();
                    prediction.setScore(score * 1.5f); //score + 50% of score
                }
            }
        }
        return predictions;
    }

}
