package uk.ac.ebi.spot.zooma.exception;

/**
 * Created by olgavrou on 16/03/2017.
 */
public class AnnotationNotFoundException extends RuntimeException {
    public AnnotationNotFoundException(String annotationId) {
        super("Annotation with id: " + annotationId + " does not exist!");
    }
}
