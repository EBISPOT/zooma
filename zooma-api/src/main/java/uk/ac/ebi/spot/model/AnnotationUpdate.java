package uk.ac.ebi.spot.model;


import java.net.URI;
import java.util.Collection;

/**
 * An annotation update represents a set of allowed fields that can be updated on an annotation object in Zooma.
 * Zooma has a built in provenence model so that annotation never get updated, but rather replaced by a new annotation.
 * An annotation can be updated in two ways, either the {@link uk.ac.ebi.fgpt.zooma.model.Property} is updated or
 * the set of semantic tag URIs change.
 * @author Simon Jupp
 * @date 22/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public interface AnnotationUpdate extends Update<Annotation> {

    /**
     * The set of seamantic tags to add to the new annotation, check
     * isRetainSemanticTags() to see if the previous semantic tags should be
     * retained on the new annotation
     * @return
     */
    Collection<URI> getSemanticTags();

    /**
     * The set of new semantic tags to apply to the new annotation.
     * if isRetainSemanticTags() is true then previous tags from the annotation
     * being updated will be preserved in the new annotation
     * @param semanticTags
     */
    void setSemanticTags(Collection<URI> semanticTags);

    /**
     * Get the property type change for the new annotation.
     * If null the new annotation will keep the property from the
     * previous annotation
     * @return newPropertyType can be null
     */
    String getPropertyType();

    /**
     * Set the property type change for the new annotation.
     * If null the new annotation will keep the property from the
     * previous annotation
     * @return newPropertyType can be null
     */
    void setPropertyType(String propertyType);

    /**
     * Get the property value change for the new annotation.
     * If null the new annotation will keep the property from the
     * previous annotation
     * @return newPropertyType can be null
     */
    String getPropertyValue();

    /**
     * Set the property value change for the new annotation.
     * If null the new annotation will keep the property from the
     * previous annotation
     * @return newPropertyType can be null
     */
    void setPropertyValue(String propertyValue);

    /**
     * Get if the update should retain semantic tags form the
     * annotation being updated. If true all previous annotations
     * will be transferred to the newly created annotation
     * @return if the semantic tags should be retained
     */
    boolean isRetainSemanticTags();

    /**
     * Set if the update should retain semantic tags form the
     * annotation being updated. If true all previous annotations
     * will be transferred to the newly created annotation
     */
    void setRetainSemanticTags(boolean retainSemanticTags);
}
