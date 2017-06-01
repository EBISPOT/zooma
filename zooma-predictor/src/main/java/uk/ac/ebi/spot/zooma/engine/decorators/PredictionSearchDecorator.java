package uk.ac.ebi.spot.zooma.engine.decorators;

import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
public abstract class PredictionSearchDecorator implements PredictionSearch {

    private final PredictionSearch delegate;

    public PredictionSearchDecorator(PredictionSearch delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        return delegate.search(propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        List<AnnotationPrediction> predictions = new ArrayList<>();
        if(PredictorUtils.shouldSearch(origin)) {
            predictions = delegate.searchWithOrigin(propertyValuePattern, origin, filter);
        }
        return predictions;
    }


    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        return delegate.search(propertyType, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> origin, boolean filter) {
        List<AnnotationPrediction> predictions = new ArrayList<>();
        if(PredictorUtils.shouldSearch(origin)) {
            predictions = delegate.searchWithOrigin(propertyType, propertyValuePattern, origin, filter);
        }
        return predictions;
    }

}
