package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;

import java.util.Collection;

/**
 * A data access object that defines methods to create, retrieve, update and delete annotation sources from a ZOOMA
 * datasource.
 *
 * @author Tony Burdett
 * @date 17/12/13
 */
public interface AnnotationSourceDAO extends ZoomaDAO<AnnotationSource> {
    /**
     * Retrieves a collection of annotation sources from the underlying datasource, limited to those with the given
     * source name.  Source names are likely to be unique, so you would normally expect to retrieve a collection with a
     * single element.  But, as uniqueness of source names is not guaranteed, this is why this method returns a
     * collection.
     *
     * @param sourceName the name of the annotation source to retrieve
     * @return a collection of sources with the given name
     */
    Collection<AnnotationSource> readBySourceName(String sourceName);
}
