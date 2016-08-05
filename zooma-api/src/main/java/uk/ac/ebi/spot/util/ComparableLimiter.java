package uk.ac.ebi.spot.util;

import java.util.Collection;
import java.util.List;

/**
 * A generic interface for limiting a list of collection of {@link Comparable} objects to the first 'n' results.
 *
 * @author Tony Burdett
 * @date 21/03/12
 */
public interface ComparableLimiter<T extends Comparable> extends Limiter<T> {
    /**
     * Limits the supplied collection to a list of specified size.  This operation should always return a list that has
     * the same ordering when supplied the same collection as input, by first sorting the collection of {@link
     * Comparable} objects into their natural sort order before limiting.
     *
     * @param collection the collection being limited
     * @param size       the size of the resulting list
     * @return a list of size 'limit', containing a limited set of elements from the supplied collection
     */
    List<T> limit(Collection<T> collection, int size);

    /**
     * Limits the supplied list to a list of specified size, starting with the element with index specified by the start
     * parameter.  This operation should always return a list that has the same ordering when supplied the same
     * collection as input, by first sorting the collection of {@link Comparable} objects into their natural sort order
     * before limiting.
     *
     * @param collection the list being limited
     * @param size       the size of the resulting list
     * @param start      the element to start from
     * @return a list of size 'limit', containing a limited set of elements from the supplied collection
     */
    List<T> limit(Collection<T> collection, int size, int start);
}
