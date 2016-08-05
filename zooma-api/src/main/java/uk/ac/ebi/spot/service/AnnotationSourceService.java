package uk.ac.ebi.spot.service;

import uk.ac.ebi.spot.model.AnnotationSource;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows direct retrieval of {@link uk.ac.ebi.spot.model.AnnotationSource}s known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will simply delegate all lookup requests to an {@link
 * uk.ac.ebi.spot.datasource.AnnotationSourceDAO}.
 *
 * @author Tony Burdett
 * @date 17/12/13
 */
public interface AnnotationSourceService {
    /**
     * Returns all annotation sources known to ZOOMA
     *
     * @return a collection of all annotation sources
     */
    Collection<AnnotationSource> getAnnotationSources();

    /**
     * Returns the annotation source with the given name
     *
     * @param sourceName a shortname representation the name of the source
     * @return a single annotation source with the given name
     */
    AnnotationSource getAnnotationSource(String sourceName);

    /**
     * Returns the annotation source with the given URI
     *
     * @param uri the URI of the required datasource
     * @return a single annotation source with the given name
     */
    AnnotationSource getAnnotationSource(URI uri);
}

