package uk.ac.ebi.spot.zooma.engine.decorators;

import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

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
    public List<Prediction> search(String propertyValuePattern) {
        return super.search(propertyValuePattern);
    }

    @Override
    public List<Prediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        List<Prediction> predictions = super.searchWithOrigin(propertyValuePattern, origin, filter);
        if(!filter){
            return boostOrigin(predictions, origin);
        }
        return predictions;
    }

    @Override
    public List<Prediction> search(String propertyType, String propertyValuePattern) {
        return super.search(propertyType, propertyValuePattern);
    }


    @Override
    public List<Prediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> ontologies, boolean filter) {
        List<Prediction> predictions = super.searchWithOrigin(propertyType, propertyValuePattern, ontologies, filter);
        if(!filter){
            return boostOrigin(predictions, ontologies);
        }
        return predictions;

    }


    private List<Prediction> boostOrigin(List<Prediction> predictions, List<String> origin){
        for (Prediction prediction :  predictions){
            Collection<String> origins = new ArrayList<>();
            origins.addAll(((AnnotationPrediction)prediction).getSource());
            origins.addAll(((AnnotationPrediction)prediction).getTopic());
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
