package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
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
    private Collection<URI> replacingAnnotations;
    private Collection<URI> replacedAnnotations;
    private AnnotationProvenance annotationProvenance;

    public SimpleAnnotation(URI uri,
                            Collection<BiologicalEntity> biologicalEntities,
                            Property annotatedProperty,
                            AnnotationProvenance annotationProvenance,
                            URI... semanticTags) {
        this(uri, biologicalEntities, annotatedProperty, semanticTags, new URI[0], new URI[0], annotationProvenance);
    }

    public SimpleAnnotation(URI uri,
                            Collection<BiologicalEntity> biologicalEntities,
                            Property annotatedProperty,
                            URI[] semanticTags,
                            URI[] replacingAnnotations,
                            URI[] replacedAnnotations,
                            AnnotationProvenance annotationProvenance) {
        super(uri);
        this.biologicalEntities = biologicalEntities;
        this.annotatedProperty = annotatedProperty;
        this.semanticTags = new HashSet<>();
        if (semanticTags != null) {
            for (URI u : semanticTags) {
                Collections.addAll(this.semanticTags, u);
            }
        }
        this.replacingAnnotations = new HashSet<>();
        Collections.addAll(this.replacingAnnotations, replacingAnnotations);
        this.replacedAnnotations = new HashSet<>();
        Collections.addAll(this.replacedAnnotations, replacedAnnotations);
        this.annotationProvenance = annotationProvenance;
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
            this.biologicalEntities.add(biologicalEntity);
        }
    }

    public void addSemanticTag(URI semanticTag) {
        if (!semanticTags.contains(semanticTag)) {
            this.semanticTags.add(semanticTag);
        }
    }

    public void addAnnotationProvenance(AnnotationProvenance annotationProvenance) {
        // if the supplied annotation provenance has a more recent annotation date, update
        if (this.annotationProvenance.getAnnotationDate().before(annotationProvenance.getAnnotationDate())) {
            this.annotationProvenance = annotationProvenance;
        }
    }

    @Override public Collection<URI> replacedBy() {
        return replacingAnnotations;
    }

    @Override public void setReplacedBy(URI... replacedBy) {
        Collections.addAll(this.replacingAnnotations, replacedBy);
    }

    @Override public Collection<URI> replaces() {
        return replacedAnnotations;
    }

    @Override public void setReplaces(URI... replaces) {
        Collections.addAll(this.replacedAnnotations, replaces);
    }

    @Override public String toString() {
        return "SimpleAnnotation {\n" +
               "  uri='" + getURI() + "'\n" +
               "  biologicalEntities=" + biologicalEntities + "'\n" +
               "  annotatedProperty=" + annotatedProperty + "'\n" +
               "  semanticTags=" + semanticTags + "'\n" +
               "  replacingAnnotations=" + replacingAnnotations + "'\n" +
               "  replacedAnnotations=" + replacedAnnotations + "'\n" +
               "  annotationProvenance=" + annotationProvenance + "'\n" +
               '}';
    }
}
