package uk.ac.ebi.fgpt.zooma.util;

import java.util.List;

/**
 * A basic implementation of a {@link uk.ac.ebi.fgpt.zooma.util.Limiter} that limits any list of objects to the first n
 * results
 *
 * @author Tony Burdett
 * @date 04/04/12
 */
public class BasicLimiter<T> implements Limiter<T> {
    @Override public List<T> limit(List<T> ts, int size) {
        return limit(ts, size, 0);
    }

    @Override public List<T> limit(List<T> ts, int size, int start) {
        return ts.size() < size ? ts : ts.subList(start, start + size);
    }
}
