package uk.ac.ebi.fgpt.zooma.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * A basic implementation of an Annotation
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class SimpleAnnotation extends AbstractIdentifiable implements Annotation {
    private Collection<BiologicalEntity> biologicalEntities;
    private Property annotatedProperty;
    private Collection<URI> semanticTags;
    private Collection<URI> replacedBy;
    private Collection<URI> replaces;
    private AnnotationProvenance annotationProvenance;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public SimpleAnnotation(URI uri,
                            Collection<BiologicalEntity> biologicalEntities,
                            Property annotatedProperty,
                            AnnotationProvenance annotationProvenance,
                            URI... semanticTags) {
        this(uri, biologicalEntities, annotatedProperty, annotationProvenance, semanticTags, new URI[0], new URI[0]);
    }

    public SimpleAnnotation(URI uri,
                            Collection<BiologicalEntity> biologicalEntities,
                            Property annotatedProperty,
                            AnnotationProvenance annotationProvenance,
                            URI[] semanticTags,
                            URI[] replacedBy,
                            URI[] replaces) {
        super(uri);
        this.biologicalEntities = new HashSet<>();
        if (biologicalEntities != null) {
            this.biologicalEntities.addAll(biologicalEntities);
        }
        this.annotatedProperty = annotatedProperty;
        this.annotationProvenance = annotationProvenance;
        this.semanticTags = new HashSet<>();
        if (semanticTags != null) {
            Collections.addAll(this.semanticTags, semanticTags);
        }
        this.replacedBy = new HashSet<>();
        if (replacedBy != null) {
            Collections.addAll(this.replacedBy, replacedBy);
        }
        this.replaces = new HashSet<>();
        if (replaces != null) {
            Collections.addAll(this.replaces, replaces);
        }
    }

    @Override public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
        return biologicalEntities;
    }

    @Override public Property getAnnotatedProperty() {
        return annotatedProperty;
    }

    @Override public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    @Override public AnnotationProvenance getProvenance() {
        return annotationProvenance;
    }

    public void addAnnotatedBiologicalEntity(BiologicalEntity biologicalEntity) {
        if (!biologicalEntities.contains(biologicalEntity)) {
            if (getLog().isTraceEnabled()) {
                getLog().trace("Adding biological entity '" + biologicalEntity.toString() + "' " +
                                       "to " + this.biologicalEntities + " for annotation <" + getURI() + ">");
            }
            this.biologicalEntities.add(biologicalEntity);
        }
    }

    public void addSemanticTag(URI semanticTag) {
        if (!semanticTags.contains(semanticTag)) {
            if (getLog().isTraceEnabled()) {
                getLog().trace("Adding semantic tag <" + semanticTag.toString() + "> " +
                                       "to " + this.semanticTags + " for annotation <" + getURI() + ">");
            }
            this.semanticTags.add(semanticTag);
        }
    }

    public void addAnnotationProvenance(AnnotationProvenance annotationProvenance) {
        boolean updated = false;
        Date oldDate, newDate;
        AnnotationProvenance.Evidence oldEvidence, newEvidence;

        if (this.annotationProvenance != null) {
            // if the supplied annotation provenance has a more recent annotation date, update
            if (this.annotationProvenance.getAnnotationDate() != null) {
                if (annotationProvenance.getAnnotationDate() != null) {
                    oldDate = this.annotationProvenance.getAnnotationDate();
                    newDate = annotationProvenance.getAnnotationDate();
                    if (oldDate.before(newDate)) {
                        updated = true;
                        if (getLog().isTraceEnabled()) {
                            getLog().trace("Updating annotation provenance (newer annotation date) from '" +
                                                   this.annotationProvenance.toString() + "' to " +
                                                   annotationProvenance.toString() + " for " +
                                                   "annotation <" + getURI() + ">");
                        }
                        this.annotationProvenance = annotationProvenance;
                    }
                }
            }

            // if the supplied annotation provenance has a more recent generation date,
            // and wasn't already updated, then update
            if (!updated) {
                if (this.annotationProvenance.getGeneratedDate() != null) {
                    if (annotationProvenance.getGeneratedDate() != null) {
                        oldDate = this.annotationProvenance.getGeneratedDate();
                        newDate = annotationProvenance.getGeneratedDate();
                        if (oldDate.before(newDate)) {
                            updated = true;
                            if (getLog().isTraceEnabled()) {
                                getLog().trace("Updating annotation provenance (newer generated date) from '" +
                                                       this.annotationProvenance.toString() + "' to " +
                                                       annotationProvenance.toString() + " for " +
                                                       "annotation <" + getURI() + ">");
                            }
                            this.annotationProvenance = annotationProvenance;
                        }
                    }
                }
            }

            // if the supplied annotation provenance has a better evidence code,
            // and wasn't already updated by date, then update
            if (!updated) {
                if (this.annotationProvenance.getEvidence() != null) {
                    if (annotationProvenance.getEvidence() != null) {
                        oldEvidence = this.annotationProvenance.getEvidence();
                        newEvidence = annotationProvenance.getEvidence();
                        if (oldEvidence.ordinal() > newEvidence.ordinal()) {
                            if (getLog().isTraceEnabled()) {
                                getLog().trace("Updating annotation provenance (better evidence) from '" +
                                                       this.annotationProvenance.toString() + "' to " +
                                                       annotationProvenance.toString() + " " +
                                                       "for annotation <" + getURI() + ">");
                            }
                            this.annotationProvenance = annotationProvenance;
                        }
                    }
                }
            }
        }
    }

    @Override public Collection<URI> getReplacedBy() {
        return replacedBy;
    }

    @Override public void setReplacedBy(URI... replacedBy) {
        Collections.addAll(this.replacedBy, replacedBy);
    }

    @Override public Collection<URI> getReplaces() {
        return replaces;
    }

    @Override public void setReplaces(URI... replaces) {
        Collections.addAll(this.replaces, replaces);
    }

    @Override public String toString() {
        return "SimpleAnnotation {\n" +
               "  uri='" + getURI() + "'\n" +
               "  biologicalEntities=" + biologicalEntities + "'\n" +
               "  annotatedProperty=" + annotatedProperty + "'\n" +
               "  semanticTags=" + semanticTags + "'\n" +
               "  isReplacedBy=" + replacedBy + "'\n" +
               "  replaces=" + replaces + "'\n" +
               "  annotationProvenance=" + annotationProvenance + "'\n" +
               '}';
    }
}
