package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.Qualitative;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/12/13
 */
public abstract class AbstractNeedlemanWunschJaccardScorer<T extends Qualitative> extends AbstractStringQualityBasedScorer<T> {
    private NeedlemanWunch nwSimilarity;
    private JaccardSimilarity jaccardSimilarity;

    public AbstractNeedlemanWunschJaccardScorer() {
        this.nwSimilarity = new NeedlemanWunch();
        this.jaccardSimilarity = new JaccardSimilarity();
    }

    protected float getSimilarity(String s1, String s2) {
        // similarity is average NW and Jaccard score, squared
        float nw = nwSimilarity.getSimilarity(s1, s2);
        float jaccard = jaccardSimilarity.getSimilarity(s1, s2);
        return (float) Math.pow((double) (nw + jaccard) / 2, 2);
    }
}
