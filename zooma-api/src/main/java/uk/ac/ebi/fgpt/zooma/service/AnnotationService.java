package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.*;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows direct retrieval of an {@link uk.ac.ebi.fgpt.zooma.model.Annotation} or sets of
 * Annotations known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.fgpt.zooma.datasource
 * .AnnotationDAO} as and when required.  If any caching or indexing strategies are required, they should be implemented
 * here rather than at the DAO level.
 *
 * @author Tony Burdett
 * @date 23/03/12
 */
public interface AnnotationService extends Service<Annotation> {
    /**
     * Returns the collection of all annotations in ZOOMA
     *
     * @return all properties
     */
    Collection<Annotation> getAnnotations();

    /**
     * Returns a subset of all annotations in ZOOMA, limited by the size of the set and the start index.
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of annotations of size 'limit'
     */
    Collection<Annotation> getAnnotations(int limit, int start);

    /**
     * Retrieves a collection of annotations about all the biological entities within the given study.
     * <p/>
     * This method is a convenience method for <code>Collection&lt;Annotation&gt; annotations =
     * lookupService.searchByBiologicalEntity(study.getBiologicalEntities())</code>
     *
     * @param study the study to retrieve annotations for
     * @return a collection of annotations about the biological entities within this study
     */
    Collection<Annotation> getAnnotationsByStudy(Study study);

    /**
     * Retrieves a collection of annotations about the given biological entity.
     *
     * @param biologicalEntity the biological entity to fetch annotations for
     * @return a collection of annotations about this biological entities
     */
    Collection<Annotation> getAnnotationsByBiologicalEntity(BiologicalEntity biologicalEntity);

    /**
     * Retrieves a collection of annotations about the given property
     *
     * @param property the property to fetch annotations for
     * @return a collection of annotations about this property
     */
    Collection<Annotation> getAnnotationsByProperty(Property property);

    /**
     * Retrieves a collection of annotations that are about the OWLClass with the supplied IRI
     *
     * @param shortname the shortened URI (with prefix) of the semantic tag which the fetched annotations should be
     *                  about
     * @return a collections of annotations that are about the entity with the supplied URI
     */
    Collection<Annotation> getAnnotationsBySemanticTag(String shortname);

    /**
     * Retrieves a collection of annotations that are about the OWLClass with the supplied IRI
     *
     * @param semanticTagURI the URI of the semantic tag which the fetched annotations should be about
     * @return a collections of annotations that are about the entity with the supplied URI
     */
    Collection<Annotation> getAnnotationsBySemanticTag(URI semanticTagURI);

    /**
     * Returns an annotation from ZOOMA, given it's URI.
     *
     * @param shortname the shortened URI (with prefix) identifier of this annotation
     * @return the property with this URI
     */
    Annotation getAnnotation(String shortname);

    /**
     * Returns an annotation from ZOOMA, given it's URI.
     *
     * @param uri the identifier of this property
     * @return the property with this URI
     */
    Annotation getAnnotation(URI uri);

    /**
     * Saves the given annotation in ZOOMA.  Returns the updated annotation reference (which should now include an
     * assigned URI, if there wasn't one already)
     *
     * @param annotation the annotation to save (will be created if it doesn't already exist)
     * @return an updated reference to the annotation, if necessary
     * @throws ZoomaUpdateException if something went wrong during the update
     */
    Annotation saveAnnotation(Annotation annotation) throws ZoomaUpdateException;

    /**
     * Saves the given annotations in ZOOMA.  Returns the updated annotations reference (which should now include an
     * assigned URI, if there wasn't one already)
     *
     * @param annotations the annotations to save (will be created if it doesn't already exist)
     * @return an updated reference to the annotation, if necessary
     * @throws ZoomaUpdateException if something went wrong during the update
     */
    Collection<Annotation> saveAnnotations(Collection<Annotation> annotations) throws ZoomaUpdateException;

    /**
     *
     * Create a new collection of annotations which replace every annotations in the annotations to update
     * with a new property and/or new semantic tags
     *
     * @param annotationsToUpdate the annotations to be replaced
     * @param update the annotation update to be performed
     * @return A collection of newly created semantic tags
     */
    Collection<Annotation> updatePreviousAnnotations(Collection<Annotation> annotationsToUpdate, AnnotationUpdate update) throws ZoomaUpdateException;
}
