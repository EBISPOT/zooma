package uk.ac.ebi.spot.zooma.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.Scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by olgavrou on 17/05/2017.
 */
@Component("secondary.scorer")
@Primary
public class PredictionSearchScorerDecorator implements PredictionSearch {

    private PredictionSearch predictionSearch;
    private Scorer<AnnotationPrediction> scorer;

    @Autowired
    public PredictionSearchScorerDecorator(@Qualifier("filter") PredictionSearch predictionSearch,
                                           Scorer<AnnotationPrediction> scorer) {
        this.predictionSearch = predictionSearch;
        this.scorer = scorer;
    }
    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyValuePattern);
        return similarityScorer(predictions, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyValuePattern, origin, originType, exclusiveOrigins);
        return similarityScorer(predictions, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyType, propertyValuePattern);
        return similarityScorer(predictions, propertyValuePattern);
    }

    @Override
    public List<AnnotationPrediction> search(String propertyType, String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins) {
        List<AnnotationPrediction> predictions = predictionSearch.search(propertyType, propertyValuePattern, origin, originType, exclusiveOrigins);
        return similarityScorer(predictions, propertyValuePattern);
    }

    private List<AnnotationPrediction> similarityScorer(List<AnnotationPrediction> predictions, String propertyValue){
        Optional<AnnotationPrediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxNormalizedScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        //scorer based on text similarity
        Map<AnnotationPrediction, Float> annotationsToScore = scorer.score(predictions, propertyValue);

        List<AnnotationPrediction> scoredResults = new ArrayList<>();
        scoredResults.addAll(annotationsToScore.keySet());

        //normalize to 100
        scoredResults.stream().forEach(annotation -> annotation.setScore(normScore(maxNormalizedScore, annotationsToScore.get(annotation))));

        return scoredResults;
    }

    // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
    private float normScore(Float maxScoreBeforeScorer, Float scoreAfterScorer) {
        float dx = 100 * ((maxScoreBeforeScorer - scoreAfterScorer) / maxScoreBeforeScorer);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }
}
