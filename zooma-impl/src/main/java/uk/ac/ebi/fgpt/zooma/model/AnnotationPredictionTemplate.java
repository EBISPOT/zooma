package uk.ac.ebi.fgpt.zooma.model;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public interface AnnotationPredictionTemplate extends AnnotationPrediction {
    AnnotationPredictionTemplate derivedFrom(Annotation originalAnnotation);

    AnnotationPredictionTemplate searchWas(String propertyValue);

    AnnotationPredictionTemplate searchWas(String propertyValue, String propertyType);

    AnnotationPredictionTemplate confidenceIs(AnnotationPrediction.Confidence confidence);

    AnnotationPrediction build();
}
