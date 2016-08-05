package uk.ac.ebi.spot.model;

import java.net.URI;
import java.util.Collection;

/**
 * A summary object that encapsulates an overview of set of similar annotations the property it annotates, and the
 * ontology concept to which it is annotated.  Annotation summaries represent each unique combination of property type,
 * property value and ontology concept, and provide references to each annotation which uses this combination.
 * <p/>
 * Note that some property values commonly contain more than one annotation (often compound types - "heart and lung" may
 * be tagged with semantic tags for heart and lung individually).  Where this happens, this summary will show both
 * semantic tags collected together.
 * <p/>
 * Note that unlike other model objects in ZOOMA, Annotation summaries do not have a fixed type ID and name - rather, it
 * is up to implementations to assign these based on subtypes.  Implementing classes should consider the ontology term
 * that the annotation summary is about so as to present informative type information to consumers.
 *
 * @author Tony Burdett
 * @date 24/05/12
 */
public interface AnnotationSummary extends Identifiable, Qualitative {
    /**
     * Returns the type name of this annotation summary. This is normally a subtype of the general top level annotation
     * summary type name that considers the semantic tags of the annotation and represent that as part of the type.
     *
     * @return the annotation summary type name
     */
    String getAnnotationSummaryTypeName();

    /**
     * Returns the ID assigned to this annotation summary.  This will be a hex string that identifies this annotation
     * summary.  Although IDs should be unique, they are not constrained to be so globally.
     *
     * @return an ID assigned to this summary
     */
    String getID();

    /**
     * Returns the property value that was used to describe the biological entities in the series of annotations this
     * summary encapsulates.
     *
     * @return the property value text
     */
    URI getAnnotatedPropertyUri();

    /**
     * Returns the property value that was used to describe the biological entities in the series of annotations this
     * summary encapsulates.
     *
     * @return the property value text
     */
    String getAnnotatedPropertyValue();

    /**
     * Returns the property type, if any, that was used to describe the biological entities in the series of annotations
     * this summary encapsulates.  This can legitimately be null, wherever property types have not been specified.
     *
     * @return the property type text
     */
    String getAnnotatedPropertyType();

    /**
     * Returns the collection of URIs of the semantic tags that are used in this annotation summary, in no particular
     * order.
     *
     * @return the semantic tag that describes this annotation
     * @see uk.ac.ebi.spot.model.Annotation#getSemanticTags()
     */
    Collection<URI> getSemanticTags();

    /**
     * Returns a list of the URIs for the collection of annotations this summary represents.  Each annotation within the
     * resulting collection is guaranteed to contain the pattern represented by this summary (in other words, {@link
     * #getAnnotatedPropertyType()}, {@link #getAnnotatedPropertyValue()} and {@link #getSemanticTags} all refer to the
     * same entities as for each annotation in this list.
     *
     * @return the provenance associated with this annotation
     * @see Annotation
     */
    Collection<URI> getAnnotationURIs();

    /**
     * Returns a metric measuring the quality score of this annotation summary.  This considers the score of the highest
     * scoring annotation that uses this pattern, the frequency with which this pattern is used, and the number of
     * datasources that independently verify this pattern.
     *
     * @return a float representing the maximum single annotation score for this summary
     */
    float getQuality();

    /**
     * Returns the collection of sources where this annotation summary holds
     */

    Collection<URI> getAnnotationSourceURIs();
}