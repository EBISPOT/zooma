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
@Component
@Primary
public class AnnotationPredictionSearchDecorator implements AnnotationPredictionSearch{

    private AnnotationPredictionSearch delegate;

    @Autowired
    public AnnotationPredictionSearchDecorator(@Qualifier("delegate") AnnotationPredictionSearch delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        return delegate.search(propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> sources) {
        return delegate.search(propertyValuePattern, sources);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        return delegate.search(propertyType, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> sources) {
        return delegate.search(propertyType, propertyValuePattern, sources);
    }

    @Override
    public List<AnnotationPrediction> searchByPreferredSources(String propertyValuePattern, List<String> preferredSources, List<String> requiredSources) {
        return delegate.searchByPreferredSources(propertyValuePattern, preferredSources, requiredSources);
    }

    @Override
    public List<AnnotationPrediction> searchByPreferredSources(String propertyType, String propertyValuePattern, List<String> preferredSources, List<String> requiredSources) {
        return delegate.searchByPreferredSources(propertyType, propertyValuePattern, preferredSources, requiredSources);
    }
}
