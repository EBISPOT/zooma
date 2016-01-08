package uk.ac.ebi.fgpt.zooma.util;

import org.apache.lucene.search.similarities.DefaultSimilarity;

/**
 * An implementation of Lucene's similarity class.  This class ignores the coord parameter for weighting the number of
 * query terms (they'll normally be genuine 'or' queries) and does not try to normalize across queries using the
 * queryNorm parameter.
 *
 * @author Tony Burdett
 * @date 14/11/13
 */
public class ZoomaSimilarity extends DefaultSimilarity {
    @Override public float coord(int overlap, int maxOverlap) {
        return 1;
    }
}
