package uk.ac.ebi.spot.zooma.engine;

import org.springframework.data.domain.Page;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
public interface PredictionSearch {

    List<AnnotationPrediction> search(String propertyValuePattern);

    List<AnnotationPrediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter);

    List<AnnotationPrediction> search(String propertyType, String propertyValuePattern);

    List<AnnotationPrediction> searchWithOrigin(String propertyType, String propertyValuePattern,
                                                List<String> origin,
                                                boolean filter);

//    List<AnnotationPrediction> searchByPreferredSources(String propertyValuePattern,
//                                                              List<String> preferredSources,
//                                                              List<String> requiredSources);
//
//    List<AnnotationPrediction> searchByPreferredSources(String propertyType,
//                                                           String propertyValuePattern,
//                                                           List<String> preferredSources,
//                                                           List<String> requiredSources);
}
