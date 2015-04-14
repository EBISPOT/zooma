package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.Qualitative;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

/**
 * An {@link uk.ac.ebi.fgpt.zooma.util.AbstractStringQualityBasedScorer} that uses a combination of the Needleman-Wunsch
 * and Jaccard algorithms to evaluate a similarity score.
 *
 * @author Tony Burdett
 * @date 12/12/13
 */
public abstract class AbstractNeedlemanWunschJaccardScorer<T extends Qualitative>
        extends AbstractStringQualityBasedScorer<T> {
    private NeedlemanWunch nwSimilarity;
    private JaccardSimilarity jaccardSimilarity;

    public AbstractNeedlemanWunschJaccardScorer() {
        this.nwSimilarity = new NeedlemanWunch();
        this.jaccardSimilarity = new JaccardSimilarity();
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
        float nw = nwSimilarity.getSimilarity(s1, s2);
        float jaccard = jaccardSimilarity.getSimilarity(s1, s2);
        return (float) Math.pow((double) (nw + jaccard) / 2, 2);
    }
}
