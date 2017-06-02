package uk.ac.ebi.spot.zooma.engine;

import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
public interface PredictionSearch {

    List<Prediction> search(String propertyValuePattern);

    List<Prediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter);

    List<Prediction> search(String propertyType, String propertyValuePattern);

    List<Prediction> searchWithOrigin(String propertyType, String propertyValuePattern,
                                                List<String> origin,
                                                boolean filter);

//    List<T> searchByPreferredSources(String propertyValuePattern,
//                                                              List<String> preferredSources,
//                                                              List<String> requiredSources);
//
//    List<T> searchByPreferredSources(String propertyType,
//                                                           String propertyValuePattern,
//                                                           List<String> preferredSources,
//                                                           List<String> requiredSources);
}
