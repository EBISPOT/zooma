package uk.ac.ebi.spot.util;

import java.util.List;

/**
 * A generic interface for limiting a list of objects to the first 'n' results.
 *
 * @author Tony Burdett
 * @date 21/03/12
 */
public interface Limiter<T> {
    /**
     * Limits the supplied list to a list of specified size.  The limit operation should always return a list that has
     * the same ordering when supplied the same list as input.
     *
     * @param list the list being limited
     * @param size the size of the resulting list
     * @return a list of size 'limit', containing a limited set of elements from the supplied collection
     */
    List<T> limit(List<T> list, int size);

    /**
     * Limits the supplied list to a list of specified size, starting with the element with index specified by the start
     * parameter.  The limit operation should always return a list that has the same ordering when supplied the same
     * collection as input.
     *
     * @param list  the list being limited
     * @param size  the size of the resulting list
     * @param start the element to start from
     * @return a list of size 'limit', containing a limited set of elements from the supplied collection
     */
    List<T> limit(List<T> list, int size, int start);
}
