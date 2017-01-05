package uk.ac.ebi.spot.zooma.model;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA 'Annotation' represents an association between a single {@link Property}, a single {@link URI}, and
 * potentially many {@link BiologicalEntity}s.
 * <p/>
 * There are cases where a single property may be annotated to multiple OWL classes - for example, where the property
 * represents a compound (or merged) expression.  In this scenario, ZOOMA considers there to be multiple annotations
 * attached to a single property.
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public interface Annotation {
    /**
     * Returns a collection of biological entities to which this annotation applies. Annotations that are intended to
     * assert themselves in a non-context-specific manner may reference an empty collection of biological entities
     *
     * @return biological entities known to zooma that can be annotated in this way
     */
    Collection<BiologicalEntity> getBiologicalEntities();

    /**
     * Returns the property that represents the text used to describe the biological entities being annotated. Must not
     * be null.
     *
     * @return the property containing the annotated text
     */
    Property getProperty();

    /**
     * Returns a collection of URIs of the entity(ies) that formally describe the metadata captured by this annotation.
     * Usually, this will be the URI of an ontology term (strictly, an OWL class) that represents a concept alluded to
     * by the property being annotated, but can in fact by the URI to any identifiable concept (GO ids, resource from
     * identifiers.org, CHEMBL ids, or more). This can be an empty collection, and if so this implies that this
     * annotation exists but that the semantic tag is not yet known or available.
     *
     * @return the collection of URIs of entities that describe this annotation
     */
    Collection<URI> getSemanticTags();

    /**
     * Returns the provenance associated with this annotation.  This will normally capture simple information such as
     * the datasource(s) that contain this annotation, or the date, time and user that made this association.
     *
     * @return the provenance associated with this annotation
     */
    AnnotationProvenance getProvenance();

    /**
     * Returns the annotation or annotations that replaced this annotation, if a newer version was generated. If this is
     * the most recent version of this annotation, this will be an empty collection.  Note that although normally
     * annotations will only be replaced by a single annotation (due to an edit or changing ontology terms) this method
     * returns a collection to support the scenario where an ontology term is split into two more specific terms.
     *
     * @return a collection of annotations that replaced this annotation
     */
    Collection<Annotation> getReplacementAnnotations();

    /**
     * Returns the annotation or annotations that this annotation replaces, if this is a newer version.  If this is the
     * oldest version of this annotation, this will be an empty collection.  Note that although normally annotations
     * will only replace by a single annotation (due to an edit or changing ontology terms) this method returns a
     * collection to support the scenario where two ontology terms were merged into a larger one.
     *
     * @return a collection of annotations that this annotation replaces
     */
    Collection<Annotation> getReplacedAnnotations();
}
