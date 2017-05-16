package uk.ac.ebi.spot.zooma.engine;

import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.List;

/**
 * Created by olgavrou on 15/05/2017.
 */
public interface PredictionSearch {

    List<AnnotationPrediction> search(String propertyValuePattern);

    List<AnnotationPrediction> search(String propertyValuePattern, List<String> origin, String originType, boolean exclusiveOrigins);

    List<AnnotationPrediction> search(String propertyType, String propertyValuePattern);

    List<AnnotationPrediction> search(String propertyType, String propertyValuePattern,
                                      List<String> sources,
                                      String originType,
                                      boolean exclusiveOrigins);

//    List<AnnotationPrediction> searchByPreferredSources(String propertyValuePattern,
//                                                              List<String> preferredSources,
//                                                              List<String> requiredSources);
//
//    List<AnnotationPrediction> searchByPreferredSources(String propertyType,
//                                                           String propertyValuePattern,
//                                                           List<String> preferredSources,
//                                                           List<String> requiredSources);
}
