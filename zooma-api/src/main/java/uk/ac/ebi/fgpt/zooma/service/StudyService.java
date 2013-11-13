package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A service that allows direct retrieval of a {@link uk.ac.ebi.fgpt.zooma.model.Study} or sets of Studies from ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.fgpt.zooma.datasource .StudyDAO}
 * as and when required.  If any caching or indexing strategies are required, they should be implemented here rather
 * than at the DAO level.
 *
 * @author Tony Burdett
 * @date 09/07/13
 */
public interface StudyService {
    /**
     * Returns the collection of all studies in ZOOMA
     *
     * @return all studies
     */
    Collection<Study> getStudies();

    /**
     * Returns a subset of all studies in ZOOMA, limited by the size of the set and the start index
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of studies of size 'limit'
     */
    Collection<Study> getStudies(int limit, int start);

    /**
     * Returns an study from ZOOMA, given it's URI.
     *
     * @param shortname the shortened URI (with prefix) identifier of this study
     * @return the property with this URI
     */
    Study getStudy(String shortname);

    /**
     * Returns an study from ZOOMA, given it's URI.
     *
     * @param uri the identifier of this property
     * @return the property with this URI
     */
    Study getStudy(URI uri);
}
