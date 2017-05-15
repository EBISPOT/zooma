package uk.ac.ebi.spot.zooma.utils.predictor;

import uk.ac.ebi.spot.zooma.model.predictor.Scorable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the Scorer temp that can score objects based on the quality attribute.  If subclassing this
 * implementation, you must implement score methods that consider the search strings against the particular objects
 * being scored.
 *
 * @author Tony Burdett
 * @date 30/11/13
 */
public abstract class AbstractQualityBasedScorer<T extends Scorable> implements Scorer<T> {
    @Override
    public Map<T, Float> score(Collection<T> collection) {
        Map<T, Float> results = new HashMap<>();
        for (T t : collection) {
            results.put(t, t.getScore());
        }
        return results;
    }
}
