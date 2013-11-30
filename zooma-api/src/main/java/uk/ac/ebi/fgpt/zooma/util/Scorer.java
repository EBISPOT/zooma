package uk.ac.ebi.fgpt.zooma.util;

import java.util.Collection;
import java.util.Map;

/**
 * A generic interface for scoring collections of objects by quality and relevance to a given search string(both type
 * and value).
 *
 * @param <T> the type of object this sorter can sort
 * @author Tony Burdett
 * @date 30/11/13
 */
public interface Scorer<T> {
    Map<T, Float> score(Collection<T> collection);

    Map<T, Float> score(Collection<T> collection, String searchString);

    Map<T, Float> score(Collection<T> collection, String searchString, String searchType);
}
