package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Document(collection = "annotations")
public class MongoAnnotation extends MongoDocument implements Annotation {

    private Collection<BiologicalEntity> annotatedBiologicalEntities;
    private Property annotatedProperty;
    private Collection<URI> semanticTags;
    private AnnotationProvenance provenance;
    private Collection<URI> replacedBy;
    private Collection<URI> replaces;
    private boolean batchLoad;
    private float quality;


    public MongoAnnotation(Collection<BiologicalEntity> annotatedBiologicalEntities,
                           Property annotatedProperty,
                           Collection<URI> semanticTags,
                           AnnotationProvenance provenance,
                           Collection<URI> replacedBy,
                           Collection<URI> replaces, boolean batchLoad) {

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
        this.batchLoad = batchLoad;
        this.quality = calculateAnnotationQuality();
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

    public boolean isBatchLoad() {
        return batchLoad;
    }

    public void setBatchLoad(boolean batchLoad) {
        this.batchLoad = batchLoad;
    }

    public float getQuality() {
        return quality;
    }

    /**
     * Returns a float value that is the quality score for the given annotation.
     * <p/>
     * This score is evaluated by an algorithm that considers: <ul> <li>Source (e.g. Atlas, AE2, ZOOMA)</li>
     * <li>Evidence (Manually created, Inferred, etc.)</li> <li>Creator - Who made this annotation?</li> <li>Time of
     * creation - How recent is this annotation?</li> </ul>
     */
    private float calculateAnnotationQuality() throws IllegalArgumentException {

        if (this.provenance == null){
            throw new IllegalArgumentException("Provenance isn't set yet, can not calculated annotation provenance");
        }
        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = AnnotationProvenance.Evidence.values().length - this.provenance.getEvidence().ordinal();
        // creation time should then work backwards from most recent to oldest
        long age = this.provenance.getAnnotationDate().getTime();

        return (float) (evidenceScore + Math.log10(age));

    }
}
