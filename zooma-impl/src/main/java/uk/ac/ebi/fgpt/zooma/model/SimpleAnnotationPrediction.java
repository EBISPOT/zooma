package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public class SimpleAnnotationPrediction extends SimpleAnnotation implements AnnotationPrediction {
    private final Confidence confidence;

    public SimpleAnnotationPrediction(Confidence confidence,
                                      Collection<BiologicalEntity> biologicalEntities,
                                      Property annotatedProperty,
                                      AnnotationProvenance annotationProvenance,
                                      URI... semanticTags) {
        this(confidence,
             biologicalEntities,
             annotatedProperty,
             annotationProvenance,
             semanticTags,
             new URI[0],
             new URI[0]);
    }

    public SimpleAnnotationPrediction(Confidence confidence,
                                      Collection<BiologicalEntity> biologicalEntities,
                                      Property annotatedProperty,
                                      AnnotationProvenance annotationProvenance,
                                      URI[] semanticTags,
                                      URI[] replacedBy,
                                      URI[] replaces) {
        super(null, biologicalEntities, annotatedProperty, annotationProvenance, semanticTags, replacedBy, replaces);
        this.confidence = confidence;
    }

    @Override public Confidence getConfidence() {
        return confidence;
    }
}
