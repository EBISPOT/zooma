package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/08/15
 */
public class AnnotationPredictionTemplate {
    private Property searchedProperty;
    private Collection<URI> semanticTags;
    private AnnotationProvenance newProvenance;

    public AnnotationPredictionTemplate(Property searchedProperty,
                                        Collection<URI> semanticTags,
                                        AnnotationProvenance newProvenance) {
        this.searchedProperty = searchedProperty;
        this.semanticTags = semanticTags;
        this.newProvenance = newProvenance;
    }

    public AnnotationPrediction withConfidence(final AnnotationPrediction.Confidence confidence) {
        return new AnnotationPrediction() {
            @Override public Confidence getConfidence() {
                return confidence;
            }

            @Override public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
                return Collections.emptyList();
            }

            @Override public Property getAnnotatedProperty() {
                return searchedProperty;
            }

            @Override public Collection<URI> getSemanticTags() {
                return semanticTags;
            }

            @Override public AnnotationProvenance getProvenance() {
                return newProvenance;
            }

            @Override public Collection<URI> getReplacedBy() {
                return null;
            }

            @Override public void setReplacedBy(URI... replacedBy) {

            }

            @Override public Collection<URI> getReplaces() {
                return null;
            }

            @Override public void setReplaces(URI... replaces) {

            }

            @Override public URI getURI() {
                return null;
            }
        };
    }
}
