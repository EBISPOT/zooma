package uk.ac.ebi.spot.zooma.engine.decorators;

import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
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
    public List<Prediction> search(String propertyValuePattern) {
        return delegate.search(propertyValuePattern);
    }

    @Override
    public List<Prediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        List<Prediction> predictions = new ArrayList<>();
        if(PredictorUtils.shouldSearch(origin)) {
            predictions = delegate.searchWithOrigin(propertyValuePattern, origin, filter);
        }
        return predictions;
    }


    @Override
    public List<Prediction> search(String propertyType, String propertyValuePattern) {
        return delegate.search(propertyType, propertyValuePattern);
    }

    @Override
    public List<Prediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> origin, boolean filter) {
        List<Prediction> predictions = new ArrayList<>();
        if(PredictorUtils.shouldSearch(origin)) {
            predictions = delegate.searchWithOrigin(propertyType, propertyValuePattern, origin, filter);
        }
        return predictions;
    }

}
