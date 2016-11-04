package uk.ac.ebi.spot.service;

import uk.ac.ebi.spot.model.SimpleAnnotation;
import uk.ac.ebi.spot.model.Property;

import java.net.URI;
import java.util.Collection;

/**
 *
 * A ZOOMA service that allows direct retrieval of an {@link SimpleAnnotation} or sets of
 * Annotation Patterns known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  If any caching or indexing strategies are required, they should be
 * implemented here rather than at the DAO level.
 * @author Simon Jupp
 * @date 28/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public interface AnnotationPatternService {

    /**
     * Retrieves a collection of annotation patterns that describe annotations about the given property value, as long
     * as they have been asserted in one of the required supplied sources.
     *
     * @param property the property value to fetch annotation summaries for
     * @param sources              the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<SimpleAnnotation> readByProperty(Property property, URI... sources);


    /**
     * Retrieves a collection of annotation patterns that describe annotations about the given property value, as long
     * as they have been asserted in one of the required supplied sources.
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param sources              the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<SimpleAnnotation> search(String propertyValuePattern, URI... sources);

    /**
     * Retrieves a collection of annotation patterns that describe annotations about the given property value and type,
     * as long as they have been asserted in one of the required supplied sources.
     *
     * @param propertyType         the property type to fetch annotation patterns for
     * @param propertyValuePattern the property value to fetch annotation patterns for
     * @param sources              the URI of the datasources that patterns should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one and matching
     * type
     */
    Collection<SimpleAnnotation> search(String propertyType, String propertyValuePattern, URI... sources);

    /**
     * Retrieves a collection of annotation patterns that have been annotated with the give semantit tag/
     *
     * @param semanticTagURI the semantic tag to search patterns by
     * @param sources              the URI of the datasources that patterns should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one and matching
     * type
     */
    Collection<SimpleAnnotation> readBySemanticTag(URI semanticTagURI, URI... sources);

    /**
     * Retrieves all annotation patterns for the given datasources
     * @param sources retrieve all patterns for the supplied sources
     */
    Collection<SimpleAnnotation> read(URI... sources);

    /**
     * Retrieves all annotation patterns
     */
    Collection<SimpleAnnotation> read();


}