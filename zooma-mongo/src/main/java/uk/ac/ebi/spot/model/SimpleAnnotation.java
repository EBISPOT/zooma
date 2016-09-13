package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Document(collection = "annotations")
public class SimpleAnnotation extends SimpleDocument implements Annotation {

    private Collection<BiologicalEntity> annotatedBiologicalEntities;
    private Property annotatedProperty;
    private Collection<URI> semanticTags;
    private AnnotationProvenance provenance;
    private Collection<URI> replacedBy;
    private Collection<URI> replaces;


    public SimpleAnnotation(String id, Collection<BiologicalEntity> annotatedBiologicalEntities,
                            Property annotatedProperty,
                            Collection<URI> semanticTags,
                            AnnotationProvenance provenance,
                            Collection<URI> replacedBy,
                            Collection<URI> replaces) {

        super(id);
        this.annotatedBiologicalEntities = new HashSet<>();
        if (annotatedBiologicalEntities != null) {
            this.annotatedBiologicalEntities.addAll(annotatedBiologicalEntities);
        }
        this.annotatedProperty = annotatedProperty;
        this.semanticTags = new HashSet<>();
        if (semanticTags != null) {
            this.semanticTags.addAll(semanticTags);
        }
        this.provenance = provenance;
        this.replacedBy = new HashSet<>();
        if (replacedBy != null) {
            this.replacedBy.addAll(replacedBy);
        }
        if (replaces != null) {
            this.replaces.addAll(replaces);
        }
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


}
