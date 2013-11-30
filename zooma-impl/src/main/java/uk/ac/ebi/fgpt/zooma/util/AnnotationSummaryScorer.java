package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Scores annotation summaries based on their quality and a lexical match measure (using a combination of
 * Needleman-Wunsch and Jaccard algorithms) to derive the score.
 *
 * @author Tony Burdett
 * @date 30/11/13
 */
public class AnnotationSummaryScorer extends AbstractQualityBasedScorer<AnnotationSummary> {
    @Override
    public Map<AnnotationSummary, Float> score(Collection<AnnotationSummary> collection, String searchString) {
        Map<AnnotationSummary, Float> results = new HashMap<>();
        for (AnnotationSummary summary : collection) {
            results.put(summary, summary.getQuality() * getSimilarity(searchString,
                                                                      summary.getAnnotatedPropertyValue()));
        }
        return results;
    }

    @Override
    public Map<AnnotationSummary, Float> score(Collection<AnnotationSummary> collection,
                                               String searchString,
                                               String searchType) {
        return score(collection, searchString);
    }

    private float getSimilarity(String s1, String s2) {
        // todo - implement this to consider string similarity using appropriate algorithm
        return 1.0f;

        // possible scoring implementation...
//        for (AnnotationSummary as : modifiedResults.keySet()) {
//            if (results.containsKey(as)) {
//                // results already contains this result
//                float previousScore = results.get(as);
//                // so calculate the weight of the lexical score based on similarity
//                float newScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);
//
//                if (newScore > previousScore) {
//                    // if the lexical score is higher than the zooma score, override zooma result
//                    results.put(as, newScore);
//                }
//            }
//            else {
//                // add the result, and weight the lexical score based on similarity
//                results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
//            }
//        }
    }
}
