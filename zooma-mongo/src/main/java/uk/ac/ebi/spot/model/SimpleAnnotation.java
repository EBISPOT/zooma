package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.cascade.CascadeSave;

import java.net.URI;
import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Document(collection = "annotations")
public class SimpleAnnotation extends SimpleDocument implements Annotation {

    @DBRef
    @CascadeSave
    private Collection<BiologicalEntity> annotatedBiologicalEntities;
    private Property annotatedProperty;
    private Collection<URI> semanticTags;
    @DBRef
    @CascadeSave
    private AnnotationProvenance provenance;
    private Collection<URI> replacedBy;
    private Collection<URI> replaces;
    private URI uri;


    public SimpleAnnotation(Collection<BiologicalEntity> annotatedBiologicalEntities,
                            Property annotatedProperty,
                            Collection<URI> semanticTags,
                            AnnotationProvenance provenance,
                            Collection<URI> replacedBy,
                            Collection<URI> replaces, URI uri) {

        this.annotatedBiologicalEntities = annotatedBiologicalEntities;
        this.annotatedProperty = annotatedProperty;
        this.semanticTags = semanticTags;
        this.provenance = provenance;
        this.replacedBy = replacedBy;
        this.replaces = replaces;
        this.uri = uri;
    }

    @Override
    public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
        return annotatedBiologicalEntities;
    }

    public void setAnnotatedBiologicalEntities(Collection<BiologicalEntity> annotatedBiologicalEntities) {
        this.annotatedBiologicalEntities = annotatedBiologicalEntities;
    }

    @Override
    public Property getAnnotatedProperty() {
        return annotatedProperty;
    }

    public void setAnnotatedProperty(Property annotatedProperty) {
        this.annotatedProperty = annotatedProperty;
    }

    @Override
    public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    public void setSemanticTags(Collection<URI> semanticTags) {
        this.semanticTags = semanticTags;
    }

    @Override
    public AnnotationProvenance getProvenance() {
        return provenance;
    }

    public void setProvenance(AnnotationProvenance provenance) {
        this.provenance = provenance;
    }

    @Override
    public Collection<URI> getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(Collection<URI> replacedBy) {
        this.replacedBy = replacedBy;
    }

    @Override
    public Collection<URI> getReplaces() {
        return replaces;
    }

    @Override
    public void setReplaces(Collection<URI> replaces) {
        this.replaces = replaces;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    public void setURI (URI uri) {
        this.uri = uri;
    }


}
