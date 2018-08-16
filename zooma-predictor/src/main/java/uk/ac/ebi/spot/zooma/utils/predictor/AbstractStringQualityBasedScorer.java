package uk.ac.ebi.spot.zooma.utils.predictor;


import uk.ac.ebi.spot.zooma.model.predictor.Scorable;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A refinement of the {@link AbstractQualityBasedScorer} abstract class that defines methods
 * to extract a string from matched objects and evaluate it's similarity to the supplied search string
 *
 * @author Tony Burdett
 * @date 12/12/13
 */
public abstract class AbstractStringQualityBasedScorer<T extends Scorable> extends AbstractQualityBasedScorer<T> {

    @Override
    public Map<T, Float> score(Collection<T> collection, String searchString) {
        Map<T, Float> results = new IdentityHashMap<T, Float>();
        for (T t : collection) {
            float q = t.getScore();
            float s = getSimilarityIgnoreCase(searchString, extractMatchedString(t));
            results.put(t, t.getScore() * getSimilarityIgnoreCase(searchString, extractMatchedString(t)));
        }
        return results;
    }

    @Override
    public Map<T, Float> score(Collection<T> collection,
                               String searchString,
                               String searchType) {
        return score(collection, searchString);
    }

    public Map<T, Float> scoreCaseSensitive(Collection<T> collection, String searchString) {
        Map<T, Float> results = new HashMap<>();
        for (T t : collection) {
            results.put(t, t.getScore() * getSimilarity(searchString, extractMatchedString(t)));
        }
        return results;
    }

    public Map<T, Float> scoreCaseSensitive(Collection<T> collection,
                                            String searchString,
                                            String searchType) {
        return scoreCaseSensitive(collection, searchString);
    }


    /**
     * Returns a string from the supplied matched object that can be used in a measure of similarity
     *
     * @param matched the object that is being scored
     * @return a string that can be compared to the search string to evaluate similarity
     */
    protected abstract String extractMatchedString(T matched);

    /**
     * Returns a float that represents the similarity between two strings
     *
     * @param searchString  the string that was used in a search
     * @param matchedString the string, extracted from a matched object, to use in a similarity comparison
     * @return a similarity score
     */
    protected abstract float getSimilarity(String searchString, String matchedString);

    /**
     * Returns a float that represents the similarity between two strings, assuming case is not important.  Calling
     * this method should be equivalent to calling {@link #getSimilarity(String, String)} after first lowercasing
     * both strings.
     *
     * @param searchString  the string that was used in a search
     * @param matchedString the string, extracted from a matched object, to use in a similarity comparison
     * @return a similarity score
     */
    protected float getSimilarityIgnoreCase(String searchString, String matchedString) {
        return getSimilarity(searchString.toLowerCase(), matchedString.toLowerCase());
    }
}
