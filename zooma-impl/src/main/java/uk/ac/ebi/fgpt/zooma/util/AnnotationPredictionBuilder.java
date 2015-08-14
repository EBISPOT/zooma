package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPredictionTemplate;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/08/15
 */
public class AnnotationPredictionBuilder {
    public static AnnotationPredictionTemplate predictFromAnnotation(Annotation annotation) {
        return new AnnotationPredictionTemplate();
    }
}
