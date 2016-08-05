package uk.ac.ebi.spot.util;

import java.util.Collection;

/**
 * A provider of {@link SearchStringProcessor}s that enables the number of eligible processors to be filtered based on a
 * query string.
 *
 * @author Tony Burdett
 * @date 19/11/13
 */
public interface SearchStringProcessorProvider {
    /**
     * Registers a {@link SearchStringProcessor} with this provider.  This processor becomes available in all unfiltered
     * contexts
     *
     * @param processor the processor to register
     */
    void registerProcessor(SearchStringProcessor processor);

    /**
     * Registers a {@link SearchStringProcessor} with this provider, and designates it available in filtered contexts
     *
     * @param processor the processor to register
     * @param filter    the filter string that applies to this processor
     */
    void registerFilteredProcessor(SearchStringProcessor processor, String filter);

    /**
     * Returns a collection of all registered search string processors.
     *
     * @return all registered search string processors
     */
    Collection<SearchStringProcessor> getProcessors();

    /**
     * Returns a collection of search string processors, filtered by the supplied criteria.  Essentially this can be
     * used to perform a mapping between eligible processors and a search string
     *
     * @param filter the string that can be used to restrict to a subset of known processors
     * @return a collection of eligible processors
     */
    Collection<SearchStringProcessor> getFilteredProcessors(String filter);
}
