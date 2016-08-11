package uk.ac.ebi.spot.model;

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
     * Returns a collection of biological entities to which this annotation applies
     *
     * @return biological entities known to zooma that can be annotated in this way
     */
    Collection<BiologicalEntity> getAnnotatedBiologicalEntities();

    /**
     * Returns the property that represents the text used to describe the biological entities being annotated
     *
     * @return the property containing the annotated text
     */
    Property getAnnotatedProperty();

    /**
     * Returns the URI of the entity that formally describes the metadata captured by this annotation.  Usually, this
     * will be the URI of an OWL class that represents a concept alluded to by the property being annotated, but can in
     * fact by the URI to any identifiable concept (GO ids, resource from identifiers.org, CHEMBL ids, or more).
     *
     * @return the OWLClass that describes this annotation
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
     * Returns the URI of the annotation or annotations that replaced this annotation, if a newer version was generated.
     * If this is the most recent version of this annotation, this can be null.  Note that although normally annotations
     * will only be replaced by a single annotation (due to an edit or changing ontology terms) this method returns a
     * collection to support the scenario where an ontology term is split into two more specific terms.
     *
     * @return a collection of URIs identifying the annotations that replaced this annotation
     */
    Collection<URI> getReplacedBy();

    /**
     * Designates the URIs specified as annotations that replace this annotation
     *
     * @param replacedBy the URIs of any annotations that replace this annotation
     */
    void setReplacedBy(Collection<URI> replacedBy);

    /**
     * Returns the annotation or annotations that this annotation replaces, if this is a newer version.  If this is the
     * oldest version of this annotation, this will be null.  Note that although normally annotations will only replace
     * by a single annotation (due to an edit or changing ontology terms) this method returns a collection to support
     * the scenario where two ontology terms were merged into a larger one.
     *
     * @return a collection of URIs identifying the annotations that this annotation replaces
     */
    Collection<URI> getReplaces();

    /**
     * Designates the URIs specified as annotations that were replaced by this annotation
     *
     * @param replaces the URIs of any annotations that were replaced by this annotation
     */
    void setReplaces(Collection<URI> replaces);
}
