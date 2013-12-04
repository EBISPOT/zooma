package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * A ZOOMA service that allows direct retrieval of {@link uk.ac.ebi.fgpt.zooma.model.AnnotationSummary} objects (that
 * summarize similar groupings of {@link uk.ac.ebi.fgpt.zooma.model.Annotation}s) known to ZOOMA.
 * <p/>
 *
 * @author Tony Burdett
 * @date 24/05/12
 */
public interface AnnotationSummarySearchService {
    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> search(String propertyValuePattern);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value, as long
     * as they have been asserted in one of the required supplied sources.
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param sources              the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> search(String propertyValuePattern, URI... sources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param propertyType         the property type to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given one and matching
     * type
     */
    Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type,
     * as long as they have been asserted in one of the required supplied sources.
     *
     * @param propertyType         the property type to fetch annotation summaries for
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param sources              the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one and matching
     * type
     */
    Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern, URI... sources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given prefix
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value, as long
     * as they have been asserted in one of the supplied sources
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @param sources             the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given prefix
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix, URI... sources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @param propertyType        the property type to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given prefix and
     * matching type
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type,
     * as long as they have been asserted in the supplied source
     *
     *
     * @param propertyType        the property type to fetch annotation summaries for
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @param sources              the URI of the datasource that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given prefix and
     * matching type
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix, URI... sources);

    /**
     * Search the set of annotation summaries in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     *
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<AnnotationSummary> searchBySemanticTags(String... semanticTagShortnames);

    /**
     * Search the set of annotation summaries in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     *
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<AnnotationSummary> searchBySemanticTags(URI... semanticTags);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value, using
     * the list of supplied sources as a ranking order.
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param rankedSources        the preferred order of AnnotationSummary datasources
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern, List<URI> rankedSources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type,
     * using the list of supplied sources as a ranking order.
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param rankedSources        the preferred order of AnnotationSummary datasources
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> searchByPreferredSources(String propertyType,
                                                           String propertyValuePattern,
                                                           List<URI> rankedSources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value, using
     * the list of supplied sources as a ranking order, as long as they have been asserted in one of the required
     * sources
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param rankedSources        the preferred order of AnnotationSummary datasources
     * @param requiredSources      the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern,
                                                           List<URI> rankedSources,
                                                           URI... requiredSources);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type,
     * using the list of supplied sources as a ranking order, as long as they have been asserted in one of the required
     * sources
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param rankedSources        the preferred order of AnnotationSummary datasources
     * @param requiredSources      the URI of the datasources that AnnotationSummaries should be present in
     * @return a collection of annotation summaries about the property with a value matching the given one
     */
    Collection<AnnotationSummary> searchByPreferredSources(String propertyType,
                                                           String propertyValuePattern,
                                                           List<URI> rankedSources,
                                                           URI... requiredSources);
}
