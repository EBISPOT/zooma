package uk.ac.ebi.spot.zooma.utils;

import uk.ac.ebi.spot.zooma.model.AnnotationPrediction;

import java.util.*;


/**
 * Some general ZOOMA utility functions
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class PredictorUtils {

    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     * <p/>
     * This form does not take a minimum score - this is equivalent to calling {@link
     * #filterAnnotationSummaries(Map, float, float)} with a cutoff score of 0.
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffPercentage the maximum distance away from the top score an annotation is allowed to be whilst not
     *                         being filtered
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<AnnotationPrediction> filterAnnotationSummaries(Map<AnnotationPrediction, Float> summaries,
                                                                    float cutoffPercentage) {
        return filterAnnotationSummaries(summaries, 0, cutoffPercentage);

    }

    public static AnnotationPrediction.Confidence getConfidence(List<AnnotationPrediction> results, float cutoffScore) {

        boolean achievedScore = false;
        for (AnnotationPrediction summary : results) {
            if (!achievedScore && summary.getQuality() > cutoffScore) {
                achievedScore = true;
                break; //won't come in here again
            }
        }

        if (results.size() == 1 && achievedScore) {
            // one good annotation, so create prediction with high confidence
            return AnnotationPrediction.Confidence.HIGH;
        }
        else {
            if (achievedScore) {
                // multiple annotations each with a good score, create predictions with good confidence
                return AnnotationPrediction.Confidence.GOOD;
            }
            else {
                if (results.size() == 1) {
                    // single stand out annotation that didn't achieve score, create prediction with good confidence
                    return AnnotationPrediction.Confidence.GOOD;
                }
                else {
                    // multiple annotations, none reached score, so create prediction with medium confidence
                    return AnnotationPrediction.Confidence.MEDIUM;
                }
            }
        }
    }


    /**
     * Filter the supplied map of annotation summaries to their score, reducing them down to a set of summaries that
     * exclude any unreasonable matches.  Summaries are excluded from the results with the following criteria: <ol>
     * <li>If it duplicates a prior summary (meaning it produces a mapping to the same collection of semantic tags)</li>
     * <li>If the score for this summary is less than the value of cutoffPercentage the value of the top scoring one.
     * So, if you supply a cutoffPercentage of 0.95, only summaries with a score of 95% the value of the top hit will be
     * included.</li> </ol>
     *
     * @param summaries        the set of summaries to filter
     * @param cutoffScore      the minimum allowed score for an annotation to not be filtered
     * @param cutoffPercentage the maximum
     * @return a filtered set of annotations, only including those that scored inside the confidence interval
     */
    public static List<AnnotationPrediction> filterAnnotationSummaries(final Map<AnnotationPrediction, Float> summaries,
                                                                    float cutoffScore,
                                                                    float cutoffPercentage) {
        if(summaries.isEmpty()){
            return new ArrayList<>();
        }
        Iterator<AnnotationPrediction> summaryIterator = summaries.keySet().iterator();

        // we need to find summaries that agree and exclude duplicates - build a reference set
        List<AnnotationPrediction> referenceSummaries = new ArrayList<>();
        referenceSummaries.add(summaryIterator.next()); // first summary can't duplicate anything

        // compare each summary with the reference set
        while (summaryIterator.hasNext()) {
            AnnotationPrediction nextSummary = summaryIterator.next();
            boolean isDuplicate = false;
            AnnotationPrediction shouldReplace = null;
            for (AnnotationPrediction referenceSummary : referenceSummaries) {
                if (allEquals(referenceSummary.getSemanticTag(), nextSummary.getSemanticTag())) {
                    isDuplicate = true;
                    if (summaries.get(nextSummary) > summaries.get(referenceSummary)) {
                        shouldReplace = referenceSummary;
                    }
                    break;
                }
            }

            // if this doesn't duplicate another summary, add to reference set
            if (!isDuplicate) {
                referenceSummaries.add(nextSummary);
            }
            else {
                // duplicate, is the new one better?
                if (shouldReplace != null) {
                    //try and replace, keeping the order that they where placed in
                    for (int i = 0; i < referenceSummaries.size(); i++) {
                        AnnotationPrediction summary = referenceSummaries.get(i);
                        if (summary.equals(shouldReplace)) {
                            referenceSummaries.remove(i);
                            referenceSummaries.add(i, nextSummary);
                        }
                    }
                }
            }
        }

        // return top scored summary
        List<AnnotationPrediction> results = new ArrayList<>();
        float topScore = Collections.max(summaries.values());
        for (AnnotationPrediction as : referenceSummaries) {
            float score = summaries.get(as);
            // if the score for this summary is within 5% of the top score,
            // AND if it is greater than the cutoff score
            // include
            if (score > (topScore * cutoffPercentage) && score >= cutoffScore) {
                results.add(as);
            }
        }

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationPrediction>() {
            @Override public int compare(AnnotationPrediction o1, AnnotationPrediction o2) {
                return summaries.get(o2).compareTo(summaries.get(o1));
            }
        });




        return results;
    }

    /**
     * Tests the contents of two collections to determine if they are equal.  This method will return true if and only
     * if all items in collection 1 are present in collection 2 and all items in collection 2 are present in collection
     * 1.  Furthermore, for collections that may contain duplicates (such as {@link List}s), both lists must be the same
     * length for this to be true.
     *
     * @param c1  collection 1
     * @param c2  collection 2
     * @param <T> the type of collection 1 and 2 (if either collection is typed, both collections must have the same
     *            type)
     * @return true if the contents of collection 1 and 2 are identical
     */
    public static <T> boolean allEquals(Collection<T> c1, Collection<T> c2) {
        // quick size screen for sets - if sizes aren't equal contents definitely can't be
        if (c1 instanceof Set && c2 instanceof Set) {
            if (c1.size() != c2.size()) {
                return false;
            }
        }

        // either both c1 and c2 are not a set or both sets and sizes are equal

        // is every element in c1 also in c2?
        for (T t : c1) {
            if (!c2.contains(t)) {
                return false;
            }
        }

        // and, is every element in c2 also in c1?
        for (T t : c2) {
            if (!c1.contains(t)) {
                return false;
            }
        }

        // if we get to here, all elements in each set are also in the other, so all elements are equal
        return true;
    }
}
