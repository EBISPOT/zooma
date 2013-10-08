package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Annotation;

import java.util.Collection;
import java.util.Map;

/**
 * A ZOOMA service that allows searching over the set of {@link Annotation}s known to ZOOMA.  There are two main types
 * of search: those that search all annotation text (which may include property names, annotation names, URIs and
 * provenance information) for possible matches, and those which constrain the search to annotated property names.
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public interface AnnotationSearchService {
    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * pattern.
     * <p/>
     * Implementations are free to define their own search algorithms for text matching.
     *
     * @param propertyValuePattern annotations containing this property value should be searched for
     * @return a collection of annotations containing matching property values
     */
    Collection<Annotation> search(String propertyValuePattern);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * pattern. The set of results should only include those which exactly map the supplied property type.
     * <p/>
     * Implementations are free to define their own search algorithms for text matching.
     *
     * @param propertyType         annotations containing this property type should be searched for
     * @param propertyValuePattern annotations containing this property value should be searched for
     * @return a collection of annotations containing matching property type and values
     */
    Collection<Annotation> search(String propertyType, String propertyValuePattern);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * prefix.
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     *
     * @param propertyValuePrefix annotations containing this property value prefix should be searched for
     * @return a collection of annotations containing matching property type and values
     */
    Collection<Annotation> searchPrefix(String propertyValuePrefix);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * prefix. The set of results should only include those which exactly map the supplied type.  To expand the search
     * based on typing information, first use a {@link PropertyTypeSearchService} to find possible matches for the
     * property type.
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     *
     * @param propertyType        annotations containing this property type should be searched for
     * @param propertyValuePrefix annotations containing this property value prefix should be searched for
     * @return a collection of annotations containing matching property type and values
     */
    Collection<Annotation> searchPrefix(String propertyType, String propertyValuePrefix);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * pattern.
     * <p/>
     * Implementations are free to define their own search algorithms for text matching.
     * <p/>
     * This form returns a map of matching annotations linked to a metric that describes the quality of the match.  You
     * may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePattern annotations containing this property value should be searched for
     * @return a map of annotations containing matching property type and values, associated with a score
     */
    Map<Annotation, Float> searchAndScore(String propertyValuePattern);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * pattern. The set of results should only include those which exactly map the supplied property type.
     * <p/>
     * Implementations are free to define their own search algorithms for text matching.
     * <p/>
     * This form returns a map of matching annotations linked to a metric that describes the quality of the match.  You
     * may need to sort results based on their score to determine the best match order
     *
     * @param propertyType         annotations containing this property type should be searched for
     * @param propertyValuePattern annotations containing this property value should be searched for
     * @return a map of annotations containing matching property type and values, associated with a score
     */
    Map<Annotation, Float> searchAndScore(String propertyType, String propertyValuePattern);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * prefix.
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     * <p/>
     * This form returns a map of matching annotations linked to a metric that describes the quality of the match.  You
     * may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePrefix annotations containing this property value prefix should be searched for
     * @return a map of annotations containing matching property type and values, associated with a score
     */
    Map<Annotation, Float> searchAndScoreByPrefix(String propertyValuePrefix);

    /**
     * Search the set of annotations known to ZOOMA for those which have a property value that matches the supplied
     * prefix. The set of results should only include those which exactly map the supplied type.  To expand the search
     * tbased on typing information, first use a {@link PropertyTypeSearchService} to find possible matches for the
     * property type.
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     * <p/>
     * This form returns a map of matching annotations linked to a metric that describes the quality of the match.  You
     * may need to sort results based on their score to determine the best match order
     *
     * @param propertyType        annotations containing this property type should be searched for
     * @param propertyValuePrefix annotations containing this property value prefix should be searched for
     * @return a map of annotations containing matching property type and values, associated with a score
     */
    Map<Annotation, Float> searchAndScoreByPrefix(String propertyType, String propertyValuePrefix);
}
