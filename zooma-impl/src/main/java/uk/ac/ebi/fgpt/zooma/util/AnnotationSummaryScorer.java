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
public class AnnotationSummaryScorer extends AbstractNeedlemanWunschJaccardScorer<AnnotationSummary> {
    @Override protected String extractMatchedString(AnnotationSummary matched) {
        return matched.getAnnotatedPropertyValue();
    }
}
