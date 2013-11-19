package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

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
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param propertyType         the property type to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given one and matching
     *         type
     */
    Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given prefix
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and type
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @param propertyType        the property type to fetch annotation summaries for
     * @return a collection of annotation summaries about the property with a value matching the given prefix and
     *         matching type
     */
    Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix);

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
     * Retrieves a collection of annotation summaries that describe annotations about the given property value.
     * <p/>
     * This form returns a map of matching annotation summaries linked to a metric that describes the quality of the
     * match.  You may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @return a map of annotation summaries about this property, linked to the score for the match
     */
    Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and
     * type.
     * <p/>
     * This form returns a map of matching annotation summaries linked to a metric that describes the quality of the
     * match.  You may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePattern the property value to fetch annotation summaries for
     * @param propertyType         the property type to fetch annotation summaries for
     * @return a map of annotation summaries about this property, linked to the score for the match
     */
    Map<AnnotationSummary, Float> searchAndScore(String propertyType, String propertyValuePattern);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value.
     * <p/>
     * This form returns a map of matching annotation summaries linked to a metric that describes the quality of the
     * match.  You may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @return a map of annotation summaries about this property, linked to the score for the match
     */
    Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyValuePrefix);

    /**
     * Retrieves a collection of annotation summaries that describe annotations about the given property value and
     * type.
     * <p/>
     * This form returns a map of matching annotation summaries linked to a metric that describes the quality of the
     * match.  You may need to sort results based on their score to determine the best match order
     *
     * @param propertyValuePrefix the property value to fetch annotation summaries for
     * @param propertyType        the property type to fetch annotation summaries for
     * @return a map of annotation summaries about this property, linked to the score for the match
     */
    Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyType, String propertyValuePrefix);


    /**
     * Search the set of annotation summaries in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     * <p/>
     * This form returns a map of matching annotation summaries linked to a metric that describes the quality of the
     * match.  You may need to sort results based on their score to determine the best match order
     *
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames);

    /**
     * Search the set of annotation summaries in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     *
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(URI... semanticTags);
}
