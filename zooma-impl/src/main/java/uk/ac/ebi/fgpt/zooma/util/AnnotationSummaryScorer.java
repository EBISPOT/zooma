package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Scores annotation summaries based on their quality and a lexical match measure (using a combination of
 * Needleman-Wunsch and Jaccard algorithms) to derive the score.
 *
 * @author Tony Burdett
 * @date 30/11/13
 */
public class AnnotationSummaryScorer extends AbstractQualityBasedScorer<AnnotationSummary> {
    private NeedlemanWunch nwSimilarity;
    private JaccardSimilarity jaccardSimilarity;

    public AnnotationSummaryScorer() {
        this.nwSimilarity = new NeedlemanWunch();
        this.jaccardSimilarity = new JaccardSimilarity();
    }

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
        // similarity is average NW and Jaccard score, squared
        float nw = nwSimilarity.getSimilarity(s1, s2);
        float jaccard = jaccardSimilarity.getSimilarity(s1, s2);
        return (float) Math.pow((double) (nw + jaccard) / 2, 2);
    }
}
