package uk.ac.ebi.spot.service;


import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A service that allows direct retrieval of a {@link uk.ac.ebi.spot.model.BiologicalEntity} or sets of
 * BiologicalEntities from ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.spot.datasource
 * .BiologicalEntityDAO} as and when required.  If any caching or indexing strategies are required, they should be
 * implemented here rather than at the DAO level.
 *
 * @author Tony Burdett
 * @date 09/07/13
 */
public interface BiologicalEntityService {
    /**
     * Returns the collection of all biological entities in ZOOMA
     *
     * @return all properties
     */
    Collection<BiologicalEntity> getBiologicalEntities();

    /**
     * Returns a subset of all biological entities in ZOOMA, limited by the size of the set and the start index.
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of biological entities of size 'limit'
     */
    Collection<BiologicalEntity> getBiologicalEntities(int limit, int start);

    /**
     * Retrieves a collection of biological entities about all the biological entities within the given study.
     * <p/>
     * This method is a convenience method for <code>Collection&lt;BiologicalEntity&gt; biological entities =
     * lookupService.searchByBiologicalEntity(study.getBiologicalEntities())</code>
     *
     * @param study the study to retrieve biological entities for
     * @return a collection of biological entities about the biological entities within this study
     */
    Collection<BiologicalEntity> getBiologicalEntitiesByStudy(Study study);

    /**
     * Returns an annotation from ZOOMA, given it's URI.
     *
     * @param shortname the shortened URI (with prefix) identifier of this annotation
     * @return the property with this URI
     */
    BiologicalEntity getBiologicalEntity(String shortname);

    /**
     * Returns an annotation from ZOOMA, given it's URI.
     *
     * @param uri the identifier of this property
     * @return the property with this URI
     */
    BiologicalEntity getBiologicalEntity(URI uri);
}
