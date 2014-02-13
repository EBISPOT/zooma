package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.util.Collection;

/**
 * A object that encapsulates a search against an {@link uk.ac.ebi.fgpt.zooma.service.AnnotationSummarySearchService} in
 * a command design pattern.  This allows searches to be passed around within search services, supporting reuse of code
 * to expand searches and replace the searched property value pattern
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public interface AnnotationSummarySearchCommand {
    /**
     * Execute a search using the given property value.  This may be the "original" property value, or a property value
     * that represents one of an expanded set
     *
     * @param propertyValue the property value (possibly modified) to search for
     * @return a collection of annotation summaries
     */
    Collection<AnnotationSummary> executeSearch(String propertyValue);
}
