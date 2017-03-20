package uk.ac.ebi.spot.zooma.exception;

/**
 * Created by olgavrou on 15/03/2017.
 */
public class AnnotationAlreadyExiststException extends RuntimeException {
    public AnnotationAlreadyExiststException(String annotationId) {
        super(annotationId);
    }
}
