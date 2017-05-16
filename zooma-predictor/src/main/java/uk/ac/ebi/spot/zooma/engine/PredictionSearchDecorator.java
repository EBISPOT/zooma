package uk.ac.ebi.spot.zooma.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.List;
import java.util.Optional;

/**
 * Created by olgavrou on 15/05/2017.
 */
@Component("primary.scorer")
public class PredictionSearchDecorator implements PredictionSearch {

    private PredictionSearch delegate;

    @Autowired
    public PredictionSearchDecorator(@Qualifier("delegate") PredictionSearch delegate) {
        this.delegate = delegate;
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
        int totalDocumentsFound = 0;
        for (AnnotationPrediction prediction : predictions){
            totalDocumentsFound = totalDocumentsFound + prediction.getVotes();
        }

        //effectively final
        int totDoc = totalDocumentsFound;
        Optional<AnnotationPrediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        predictions.stream().forEach(annotation -> {
            float sourceNumber = annotation.getSource().size();
            float numOfDocs = annotation.getVotes();
            float topQuality = annotation.getQuality();
            float normalizedFreq = 1.0f + (totDoc > 0 ? (numOfDocs / totDoc) : 0);
            float normalizedSolrScore = 1.0f + annotation.getScore() / maxSolrScore;
//            float score = (topQuality) * normalizedSolrScore * normalizedFreq;
            float score = (topQuality + sourceNumber) * normalizedSolrScore * normalizedFreq;
//            ParetoFunction paretoFunction = new ParetoFunction(2,1,1);
//            float paretoVotes = paretoFunction.transform(numOfDocs);
            annotation.setScore(score);
        });

        return predictions;
    }
}
