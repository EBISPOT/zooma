package uk.ac.ebi.spot.service;


import uk.ac.ebi.spot.model.AnnotationSummary;

import java.util.Collection;

/**
 * A ZOOMA service that allows direct retrieval of an {@link uk.ac.ebi.spot.model.AnnotationSummary} or sets of
 * Annotation Summaries known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.spot.datasource
 * .AnnotationSummaryDAO} as and when required.  If any caching or indexing strategies are required, they should be
 * implemented here rather than at the DAO level.
 *
 * @author Tony Burdett
 * @date 09/07/13
 */
public interface AnnotationSummaryService {
    /**
     * Returns the collection of all annotation summaries in ZOOMA
     *
     * @return all properties
     */
    Collection<AnnotationSummary> getAnnotationSummaries();

    /**
     * Returns a subset of all annotation summaries in ZOOMA, limited by the size of the set and the start index.
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of annotation summaries of size 'limit'
     */
    Collection<AnnotationSummary> getAnnotationSummaries(int limit, int start);

    /**
     * Retrieves a single annotation summary, using the ID assigned to this summary in the underlying index.
     *
     * @param annotationSummaryID the unique ID assigned to this annotation summary
     * @return a single annotation summary with this ID
     */
    AnnotationSummary getAnnotationSummary(String annotationSummaryID);
}
