package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * A data access object that defines methods to create, retrieve, update and delete annotations from a ZOOMA
 * datasource.
 *
 * @author Tony Burdett
 * @date 29/03/12
 */
public interface AnnotationDAO extends ZoomaDAO<Annotation> {
    /**
     * Retrieves a collection of annotations from a zooma datasource, limited to those which declare annotations on
     * biological entities for the supplied study.
     *
     * @param study the study declaring the required annotations
     * @return the collection of annotations declared by this study
     */
    Collection<Annotation> readByStudy(Study study);

    /**
     * Retrieves a collection of annotations from a zooma datasource, limited to those which declare annotations about
     * the supplied biological entity.
     *
     * @param biologicalEntity the entity declaring the required annotations
     * @return the collection of annotation declared to be about this entity
     */
    Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity);

    /**
     * Retrieves a collection of annotations from a zooma datasource, limited to those which declare are liunked to the
     * supplied property
     *
     * @param property the property linked to the required annotations
     * @return the collection of annotation declared to be linked to this property
     */
    Collection<Annotation> readByProperty(Property property);

    /**
     * Retrieves a collection of annotations from a zooma datasource, limited to those which are tagged by entities with
     * the given URI.  Note that the retrieved annotations may also be tagged with other entities as well as the one
     * queried by; each annotation can annotate to more than one entity, and this method will return results if the
     * supplied entity is present in the set of entities annotated against.
     *
     * @param semanticTagURI the entity which the required annotations are tagged with
     * @return the collection of annotations declared to link to the supplied ontology term
     */
    Collection<Annotation> readBySemanticTag(URI semanticTagURI);

}
