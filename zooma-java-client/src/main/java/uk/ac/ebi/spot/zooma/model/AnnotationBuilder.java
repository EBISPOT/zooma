package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/01/17
 */
public class AnnotationBuilder {
    private final AnnotationTemplate template;
    private boolean isStarted = false;

    public AnnotationBuilder() {
        this.template = new AnnotationTemplate();
    }

    public AnnotationBuilder annotation(Provenance provenance) {
        template.setProvenance(provenance);
        isStarted = true;
        return this;
    }

    public void check() throws AnnotationBuilderException {
        if (!isStarted) {
            throw new AnnotationBuilderException("Please create an annotation using the annotation() method, " +
                                                         "describing the provenance of your annotation, " +
                                                         "before performing other actions");
        }
    }

    public AnnotationBuilder fromProperty(PropertyType propertyType, PropertyValue propertyValue) {
        check();
        template.setProperty(new Property(propertyType, propertyValue));
        return this;
    }

    public AnnotationBuilder toSemanticTag(SemanticTag semanticTag) {
        check();
        template.addSemanticTag(semanticTag);
        return this;
    }

    public AnnotationBuilder inContextOf(BiologicalEntity biologicalEntity) {
        check();
        template.setBiologicalEntity(biologicalEntity);
        return this;
    }

    public Annotation build() throws AnnotationBuilderException {
        // validate first
        check();

        // if checks pass return new annotation
        return new Annotation(template.getProperty(),
                              template.getSemanticTags(),
                              template.getBiologicalEntity(),
                              template.getProvenance());
    }

    @Data
    private class AnnotationTemplate {
        private Property property;
        private BiologicalEntity biologicalEntity;
        private Provenance provenance;
        private final Collection<SemanticTag> semanticTags = new HashSet<>();

        public void addSemanticTag(SemanticTag semanticTag) {
            semanticTags.add(semanticTag);
        }
    }

    private class AnnotationBuilderException extends RuntimeException {
        public AnnotationBuilderException() {
        }

        public AnnotationBuilderException(String message) {
            super(message);
        }

        public AnnotationBuilderException(String message, Throwable cause) {
            super(message, cause);
        }

        public AnnotationBuilderException(Throwable cause) {
            super(cause);
        }

        public AnnotationBuilderException(String message,
                                          Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
