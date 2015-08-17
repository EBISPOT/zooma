package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPredictionTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public abstract class AnnotationPredictionBuilder {
    public static AnnotationPredictionTemplate buildPrediction(Annotation derivedFrom) {
        return new SimpleAnnotationPredictionTemplate(derivedFrom);
    }

    private static class SimpleAnnotationPredictionTemplate implements AnnotationPredictionTemplate {
        private Annotation derivedFrom;

        private Property property;
        private Collection<URI> semanticTags;

        private Confidence confidence;

        private SimpleAnnotationPredictionTemplate(Annotation originalAnnotation) {
            this.derivedFrom = originalAnnotation;
            this.semanticTags = originalAnnotation.getSemanticTags();
        }

        @Override public Confidence getConfidence() {
            return null;
        }

        @Override public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
            return Collections.emptyList();
        }

        @Override public Property getAnnotatedProperty() {
            return null;
        }

        @Override public Collection<URI> getSemanticTags() {
            return null;
        }

        @Override public AnnotationProvenance getProvenance() {
            return null;
        }

        @Override public Collection<URI> getReplacedBy() {
            return null;
        }

        @Override public void setReplacedBy(URI... replacedBy) {
            throw new UnsupportedOperationException("replacedBy() is not implemented for new prediction templates");
        }

        @Override public Collection<URI> getReplaces() {
            return null;
        }

        @Override public void setReplaces(URI... replaces) {
            throw new UnsupportedOperationException(
                    "replaces() is not implemented for new prediction templates.  " +
                            "If this prediction is accepted, invoke this method on the resulting annotation");
        }

        @Override public URI getURI() {
            return null;
        }

        @Override public AnnotationPredictionTemplate derivedFrom(Annotation originalAnnotation) {
            this.derivedFrom = originalAnnotation;
            return this;
        }

        @Override public AnnotationPredictionTemplate searchWas(String propertyValue) {
            this.property = new SimpleUntypedProperty(propertyValue);
            return this;
        }

        @Override public AnnotationPredictionTemplate searchWas(String propertyValue, String propertyType) {
            this.property = new SimpleTypedProperty(propertyValue, propertyType);
            return this;
        }

        @Override public AnnotationPredictionTemplate confidenceIs(Confidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public AnnotationPrediction build() {
            return new SimpleAnnotationPrediction(this.confidence,
                                                  Collections.<BiologicalEntity>emptyList(),
                                                  this.property,
                                                  this.derivedFrom.getProvenance(),
                                                  this.derivedFrom.getSemanticTags()
                                                          .toArray(new URI[this.derivedFrom.getSemanticTags().size()]));
        }
    }
}
