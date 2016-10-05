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
    private final Annotation derivedFrom;
    private final Confidence confidence;

    public SimpleAnnotationPrediction(Annotation derivedFrom,
                                      Confidence confidence,
                                      Collection<BiologicalEntity> biologicalEntities,
                                      Property annotatedProperty,
                                      AnnotationProvenance annotationProvenance,
                                      Links _links,
                                      URI... semanticTags) {
        this(derivedFrom,
             confidence,
             biologicalEntities,
             annotatedProperty,
             annotationProvenance,
             _links,
             semanticTags,
             new URI[0],
             new URI[0]);
    }

    public SimpleAnnotationPrediction(Annotation derivedFrom,
                                      Confidence confidence,
                                      Collection<BiologicalEntity> biologicalEntities,
                                      Property annotatedProperty,
                                      AnnotationProvenance annotationProvenance,
                                      Links _links,
                                      URI[] semanticTags,
                                      URI[] replacedBy,
                                      URI[] replaces) {
        super(null, biologicalEntities, annotatedProperty, annotationProvenance, _links, semanticTags, replacedBy, replaces);
        this.derivedFrom = derivedFrom;
        this.confidence = confidence;
    }

    @Override public Annotation getDerivedFrom() {
        return derivedFrom;
    }

    @Override public Confidence getConfidence() {
        return confidence;
    }
}
