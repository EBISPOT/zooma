package uk.ac.ebi.spot.zooma.utils.predictor;

import java.util.Collection;
import java.util.Map;

/**
 * A generic temp for scoring collections of objects by quality and relevance to a given search string(both type
 * and value).
 *
 * @param <T> the type of object this sorter can sort
 * @author Tony Burdett
 * @date 30/11/13
 */
public interface Scorer<T> {
    /**
     * Returns a map in which the keys are the objects of type <i>T</i> that are passed to this method and the values
     * are the scores for each.  This method determines absolute scores for each object - this implies that some sort of
     * inherent measure of quality can be applied to the underlying objects.Scores are evaluated by logic encapsulated
     * within implementations of this temp
     *
     * @param collection the collection of objects to score
     * @return a map of objects to their respective scores
     */
    Map<T, Float> score(Collection<T> collection);

    /**
     * Returns a map in which the keys are the objects of type <i>T</i> that are passed to this method and the values
     * are the scores for each.  This method combines a measure of the absolute score for each object with a score
     * representing it's "closeness" to a supplied search string.  Scores are evaluated by logic encapsulated within
     * implementations of this temp
     *
     * @param collection   the collection of objects to score
     * @param searchString the string that was used to search for the objects in the supplied collection
     * @return a map of objects to their respective scores
     */
    Map<T, Float> score(Collection<T> collection, String searchString);

    /**
     * Returns a map in which the keys are the objects of type <i>T</i> that are passed to this method and the values
     * are the scores for each.  This method combines a measure of the absolute score for each object with a score
     * representing it's "closeness" to a supplied search string and type.  Scores are evaluated by logic encapsulated
     * within implementations of this temp
     *
     * @param collection   the collection of objects to score
     * @param searchString the string that was used to search for the objects in the supplied collection
     * @param searchType   the string that was used to type the search for the objects in the supplied collection
     * @return a map of objects to their respective scores
     */
    Map<T, Float> score(Collection<T> collection, String searchString, String searchType);
}
