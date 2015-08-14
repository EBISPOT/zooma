package uk.ac.ebi.fgpt.zooma.model;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/08/15
 */
public interface AnnotationPrediction extends Annotation {
    Confidence getConfidence();

    enum Confidence {
        HIGH,
        GOOD,
        MEDIUM,
        LOW
    }
}
