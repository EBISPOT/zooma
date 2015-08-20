package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.exception.TemplateBuildingException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPredictionTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public abstract class AnnotationPredictionBuilder {
    public static AnnotationPredictionTemplate predictFromAnnotation(Annotation derivedFrom) {
        return new SimpleAnnotationPredictionTemplate(derivedFrom);
    }

    public static AnnotationPredictionTemplate predictFromSearch(Property property) {
        return new SimpleAnnotationPredictionTemplate(property);
    }

    private static class SimpleAnnotationPredictionTemplate implements AnnotationPredictionTemplate {
        private static final AnnotationSource zoomaSource =
                new SimpleDatabaseAnnotationSource(URI.create("http://www.ebi.ac.uk/spot/zooma"), "zooma");

        private Collection<BiologicalEntity> biologicalEntities;
        private Property searchedProperty;
        private Collection<URI> semanticTags;
        private AnnotationProvenance annotationProvenance;

        private Annotation derivedFrom;
        private Confidence confidence;

        private SimpleAnnotationPredictionTemplate(Annotation originalAnnotation) {
            init();
            this.searchedProperty = originalAnnotation.getAnnotatedProperty();
            this.semanticTags = originalAnnotation.getSemanticTags();
            this.derivedFrom = originalAnnotation;
        }

        public SimpleAnnotationPredictionTemplate(Property property) {
            init();
            this.searchedProperty = property;
        }

        public void init() {
            this.biologicalEntities = Collections.emptyList();
            this.semanticTags = Collections.emptyList();
            this.confidence = Confidence.LOW;
        }

        @Override public Annotation getDerivedFrom() {
            return derivedFrom;
        }

        @Override public Confidence getConfidence() {
            return confidence;
        }

        @Override public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
            return biologicalEntities;
        }

        @Override public Property getAnnotatedProperty() {
            return searchedProperty;
        }

        @Override public Collection<URI> getSemanticTags() {
            return semanticTags;
        }

        @Override public AnnotationProvenance getProvenance() {
            return annotationProvenance;
        }

        @Override public Collection<URI> getReplacedBy() {
            return Collections.emptyList();
        }

        @Override public void setReplacedBy(URI... replacedBy) {
            throw new UnsupportedOperationException("replacedBy() is not implemented for new prediction templates");
        }

        @Override public Collection<URI> getReplaces() {
            return Collections.emptyList();
        }

        @Override public void setReplaces(URI... replaces) {
            throw new UnsupportedOperationException(
                    "replaces() is not implemented for new prediction templates.  " +
                            "If this prediction is accepted, invoke this method on the resulting annotation");
        }

        @Override public URI getURI() {
            return null;
        }

        @Override public AnnotationPredictionTemplate searchWas(String propertyValue) {
            this.searchedProperty = new SimpleUntypedProperty(propertyValue);
            return this;
        }

        @Override public AnnotationPredictionTemplate searchWas(String propertyValue, String propertyType) {
            this.searchedProperty = new SimpleTypedProperty(propertyValue, propertyType);
            return this;
        }

        public AnnotationPredictionTemplate forBiologicalEntities(Collection<BiologicalEntity> biologicalEntities) {
            this.biologicalEntities = biologicalEntities;
            return this;
        }

        @Override public AnnotationPredictionTemplate derivedFrom(Annotation originalAnnotation) {
            this.derivedFrom = originalAnnotation;
            return this;
        }

        @Override public AnnotationPredictionTemplate confidenceIs(Confidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public AnnotationPrediction build() {
            if (derivedFrom != null && searchedProperty != null) {
                AnnotationProvenanceTemplate template = AnnotationProvenanceBuilder.createTemplate();

                ZoomaUser user = ZoomaUsers.getUserIfAuthenticated();
                if (user != null) {
                    template.annotatorIs(user.getFullName());
                }
                else {
                    template.annotatorIs("ZOOMA");
                }
                template.annotationDateIs(new Date()).sourceIs(zoomaSource);

                AnnotationProvenance.Evidence evidence = derivedFrom.getProvenance().getEvidence();
                AnnotationProvenance.Evidence newEvidence;
                if (evidence.equals(AnnotationProvenance.Evidence.MANUAL_CURATED)) {
                    newEvidence = AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED;
                }
                else if (evidence.compareTo(AnnotationProvenance.Evidence.SUBMITTER_PROVIDED) < 0) {
                    newEvidence = AnnotationProvenance.Evidence.COMPUTED_FROM_TEXT_MATCH;
                }
                else if (evidence.equals(AnnotationProvenance.Evidence.SUBMITTER_PROVIDED)) {
                    newEvidence = AnnotationProvenance.Evidence.NON_TRACEABLE;
                }
                else {
                    newEvidence = evidence;
                }
                this.annotationProvenance = template.evidenceIs(newEvidence).build();

                return new SimpleAnnotationPrediction(this.derivedFrom,
                                                      this.confidence,
                                                      this.biologicalEntities,
                                                      this.searchedProperty,
                                                      annotationProvenance,
                                                      this.semanticTags.toArray(
                                                              new URI[this.derivedFrom.getSemanticTags().size()]));
            }
            else {
                throw new TemplateBuildingException(
                        "Unable to create a prediction without a derived from annotation - please set " +
                                "'.derivedFrom()' before building");
            }
        }
    }
}
