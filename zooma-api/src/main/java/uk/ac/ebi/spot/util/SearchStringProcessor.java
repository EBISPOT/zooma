package uk.ac.ebi.spot.util;

import java.util.Collection;

/**
 * Encapsulates rules that allow ZOOMA search strings to be processed. How this processing is performed is up to
 * individual implementations.
 * <p/>
 * Some of the ZOOMA search services declare methods that take property value search strings and use the supplied search
 * string to query for matching annotations.  This class will return processed versions of the search string, allowing,
 * for example, partial matches to be evaluated
 *
 * @author Tony Burdett
 * @date 01/08/13
 * @see uk.ac.ebi.spot.service.AnnotationSearchService
 * @see uk.ac.ebi.spot.service.AnnotationSummarySearchService
 * @see uk.ac.ebi.spot.service.PropertySearchService
 */
public interface SearchStringProcessor {
    /**
     * Returns the boost factor for this string processor.  This must be a positive value, and should normally be below
     * 1 (as you would normally expect processed strings to score less well than unprocessed ones).  However it is
     * possible to supply a value greater than 1 - this will make processed terms more relevant than the original.
     *
     * @return the boost factor for this string processor
     */
    float getBoostFactor();

    /**
     * Returns true if this processor can process the given search string, or false otherwise.  Testing whether this
     * implementation can process the given search string before calling {@link #processSearchString(String)} avoids
     * IllegalArgumentExceptions.
     * <p/>
     * Implementations of this method should ensure that calling this method is equivalent to calling the two argument
     * form passing <code>null</code> as the type string
     *
     * @param searchString the search string to test
     * @return true if the given string can be processed using this processor
     */
    boolean canProcess(String searchString);

    /**
     * Takes a string, processes it using the rules for this implementation, and returns the processed form(s).
     *
     * @param searchString the string to process
     * @return a collection of processed forms of the search string
     * @throws IllegalArgumentException if the given search string cannot be processed by this implementation
     * @throws InterruptedException if the current thread is interrupted whilst the search string is being processed
     */
    Collection<String> processSearchString(String searchString) throws IllegalArgumentException, InterruptedException;
}
