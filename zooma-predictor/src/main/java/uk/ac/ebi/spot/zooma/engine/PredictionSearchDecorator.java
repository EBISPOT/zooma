package uk.ac.ebi.spot.zooma.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.ParetoDistributionTransformation;

import java.util.List;
import java.util.Optional;

/**
 * Created by olgavrou on 15/05/2017.
 */
@Component("primary.scorer")
public class PredictionSearchDecorator implements PredictionSearch {

    private PredictionSearch delegate;
    private ParetoDistributionTransformation transformation;

    @Autowired
    public PredictionSearchDecorator(@Qualifier("delegate") PredictionSearch delegate) {
        this.delegate = delegate;
        this.transformation = new ParetoDistributionTransformation(2);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        List<AnnotationPrediction> predictions = delegate.search(propertyValuePattern);
        return primaryMetaScore(predictions);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = delegate.search(propertyValuePattern, origin, originType, exclusiveOrigins);
        return primaryMetaScore(predictions);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        List<AnnotationPrediction> predictions = delegate.search(propertyType, propertyValuePattern);
        return primaryMetaScore(predictions);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = delegate.search(propertyType, propertyValuePattern, origin, originType, exclusiveOrigins);
        return primaryMetaScore(predictions);
    }


    private List<AnnotationPrediction> primaryMetaScore(List<AnnotationPrediction> predictions){
        Optional<AnnotationPrediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        predictions.stream().forEach(annotation -> {
            float sourceNumber = annotation.getSource().size();
            float numOfDocs = annotation.getVotes();
            float topQuality = annotation.getQuality();
            float normalizedSolrScore = 1.0f + annotation.getScore() / maxSolrScore;
            float pareto = this.transformation.transform(numOfDocs);
            float score = (topQuality + sourceNumber + pareto) * normalizedSolrScore;
            annotation.setScore(score);
        });

        return predictions;
    }
}
