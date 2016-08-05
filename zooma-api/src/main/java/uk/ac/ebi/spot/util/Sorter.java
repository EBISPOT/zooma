package uk.ac.ebi.spot.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A generic interface for sorting a collection of objects by relevance to a given search prefix and type.
 *
 * @param <T> the type of object this sorter can sort
 * @author Tony Burdett
 * @date 21/03/12
 */
public interface Sorter<T> {
    /**
     * Sorts a collection of objects into a natural sort order.  Normally, this would sort the collection of objects
     * according to their natural sort order, assuming the collection contained {@link Comparable} objects.  This is,
     * however, a recommendation rather than a requirement, unlike {@link java.util.Collections#sort(List)}.
     *
     * @param collection the collection of objects to sort
     * @return a sorted list, sorted according to the algorithm chosen by this implementation
     */
    List<T> sort(Collection<T> collection);

    /**
     * Sorts a collection of objects into a order based on a score associated with each object in the supplied map.
     * Calling this method utilises the score for sorting instead of sorting by a natural sort order.  However, if the
     * scores for two objects are the same, they may be sorted according to the outcome of comparing two objects if they
     * implement {@link Comparable}.
     *
     * @param map the map of objects to sort, where each object is associated with a score
     * @return a sorted list, sorted by score
     */
    List<T> sort(Map<T, Float> map);
}
