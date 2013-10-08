package uk.ac.ebi.fgpt.zooma.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A basic implementation of a sorter that drops a given collection of objects into a list, without modifying the basic
 * order.  If the supplied collections is itself a list, the original list is returned.  If the collection is of other
 * type, the records are dropped into a list in their natural order.
 *
 * @author Tony Burdett
 * @date 04/04/12
 */
public class UnmodifyingSorter<T> implements Sorter<T> {
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

    @Override public List<T> sort(Map<T, Float> map) {
        List<T> list = new ArrayList<>();
        if (!map.isEmpty()) {
            list.addAll(map.keySet());
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
