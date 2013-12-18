package uk.ac.ebi.fgpt.zooma.util;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

/**
 * Scores annotation summaries based on their quality and a lexical match measure (using the Needleman-Wunsch algorithm)
 * to derive the score.
 *
 * @author Tony Burdett
 * @date 18/12/13
 */
public class AnnotationSummaryNeedlemanWunschScorer extends AbstractNeedlemanWunschScorer<AnnotationSummary> {
    /**
     * Extracts the annotated property value for the matched annotation summary to compare to the search string
     *
     * @param matched the object that is being scored
     * @return the annotated property value for the given annotation summary
     */
    @Override protected String extractMatchedString(AnnotationSummary matched) {
        return matched.getAnnotatedPropertyValue();
    }
}
