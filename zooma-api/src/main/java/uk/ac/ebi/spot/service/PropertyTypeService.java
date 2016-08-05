package service;

import java.util.Collection;

/**
 * A ZOOMA service that allows direct retrieval of sets of property types known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO}
 * as and when required.  If any caching or indexing strategies are required, they should be implemented here rather
 * than at the DAO level.
 *
 * @author Tony Burdett
 * @date 23/03/12
 */
public interface PropertyTypeService {
    /**
     * Returns the collection of all properties in ZOOMA
     *
     * @return all properties
     */
    Collection<String> getPropertyTypes();

    /**
     * Returns a subset of all properties in ZOOMA, limited by the size of the set and the start index.
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of properties of size 'limit'
     */
    Collection<String> getPropertyTypes(int limit, int start);
}
