package uk.ac.ebi.spot.zooma.utils;

import org.simmetrics.metrics.NeedlemanWunch;
import uk.ac.ebi.spot.zooma.model.Qualitative;

/**
 * An {@link AbstractStringQualityBasedScorer} that uses a the Needleman-Wunsch algorithm to
 * evaluate a similarity score.
 *
 * @author Tony Burdett
 * @date 12/12/13
 */
public abstract class AbstractNeedlemanWunschScorer<T extends Qualitative> extends AbstractStringQualityBasedScorer<T> {
    private NeedlemanWunch nwSimilarity;

    public AbstractNeedlemanWunschScorer() {
        this.nwSimilarity = new NeedlemanWunch();
    }

    /**
     * Averages the Needleman-Wunsch and Jaccard scores and returns it's square as a measure of similarity.
     *
     * @param s1 the search string
     * @param s2 the matched string
     * @return a measure of similarity between the two strings
     */
    protected float getSimilarity(String s1, String s2) {
        // similarity is average NW and Jaccard score, squared
        return nwSimilarity.compare(s1, s2);
    }
}
