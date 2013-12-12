package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Qualitative;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/12/13
 */
public abstract class AbstractStringQualityBasedScorer<T extends Qualitative> extends AbstractQualityBasedScorer<T> {
    @Override
    public Map<T, Float> score(Collection<T> collection, String searchString) {
        Map<T, Float> results = new HashMap<>();
        for (T t : collection) {
            results.put(t, t.getQuality() * getSimilarity(searchString, extractMatchedString(t)));
        }
        return results;
    }

    @Override
    public Map<T, Float> score(Collection<T> collection,
                               String searchString,
                               String searchType) {
        return score(collection, searchString);
    }

    protected abstract String extractMatchedString(T matched);

    protected abstract float getSimilarity(String searchString, String matchedString);
}
