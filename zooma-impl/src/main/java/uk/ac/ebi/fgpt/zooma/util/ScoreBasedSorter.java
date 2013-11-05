package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A basic implementation of a sorter that only modifies the order of the supplied collection when a map of scores is
 * supplied.  If a list is supplied, the order is unmodified, and if a collection is supplied the contents are
 * transferred into a list in iteration order.  However, if a map is supplied the contents are sorted by score.
 *
 * @author Tony Burdett
 * @date 08/06/12
 */
public class ScoreBasedSorter<T> implements Sorter<T> {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public List<T> sort(Collection<T> ts) {
        if (ts instanceof List) {
            return (List<T>) ts;
        }
        else {
            List<T> list = new ArrayList<>();
            list.addAll(ts);
            return list;
        }
    }

    @Override public List<T> sort(final Map<T, Float> map) {
        if (map == null) {
            throw new IllegalArgumentException("Cannot sort a null map");
        }

        getLog().trace("Sorting map: " + map);
        List<T> list = new ArrayList<>();
        if (!map.isEmpty()) {
            list.addAll(map.keySet());
            Collections.sort(list, new Comparator<T>() {
                @Override public int compare(T o1, T o2) {
                    return map.get(o2).compareTo(map.get(o1));
                }
            });
            getLog().trace("Sorted contents now: " + list);
        }
        return list;
    }

    @Override public List<T> sort(Collection<T> ts, String type, String prefix) {
        return sort(ts);
    }

    @Override public List<T> sort(Map<T, Float> map, String type, String prefix) {
        return sort(map);
    }
}
