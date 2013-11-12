package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * An abstract decorator of an {@link AnnotationSummarySearchService}.  You should subclass this decorator to create
 * different decorations that add functionality to annotation summary searches.
 *
 * @author Tony Burdett
 * @date 02/08/13
 * @see AnnotationSummarySearchService
 */
public abstract class AnnotationSummarySearchServiceDecorator extends Initializable implements AnnotationSummarySearchService {
    private final AnnotationSummarySearchService _annotatationSummarySearchService;

    public AnnotationSummarySearchServiceDecorator(AnnotationSummarySearchService annotationSummarySearchService) {
        this._annotatationSummarySearchService = annotationSummarySearchService;
    }

    @Override public Collection<AnnotationSummary> search(String propertyValuePattern) {
        return _annotatationSummarySearchService.search(propertyValuePattern);
    }

    @Override public Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern) {
        return _annotatationSummarySearchService.search(propertyType, propertyValuePattern);
    }

    @Override public Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix) {
        return _annotatationSummarySearchService.searchByPrefix(propertyValuePrefix);
    }

    @Override public Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix) {
        return _annotatationSummarySearchService.searchByPrefix(propertyType, propertyValuePrefix);
    }

    @Override public Collection<AnnotationSummary> searchBySemanticTags(String... semanticTagShortnames) {
        return _annotatationSummarySearchService.searchBySemanticTags(semanticTagShortnames);
    }

    @Override public Collection<AnnotationSummary> searchBySemanticTags(URI... semanticTags) {
        return _annotatationSummarySearchService.searchBySemanticTags(semanticTags);
    }

    @Override public Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern) {
        return _annotatationSummarySearchService.searchAndScore(propertyValuePattern);
    }

    @Override public Map<AnnotationSummary, Float> searchAndScore(String propertyType, String propertyValuePattern) {
        return _annotatationSummarySearchService.searchAndScore(propertyType, propertyValuePattern);
    }
    
    @Override public Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyValuePrefix) {
        return _annotatationSummarySearchService.searchAndScoreByPrefix(propertyValuePrefix);
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreByPrefix(String propertyType,
                                                                          String propertyValuePrefix) {
        return _annotatationSummarySearchService.searchAndScoreByPrefix(propertyType, propertyValuePrefix);
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames) {
        return _annotatationSummarySearchService.searchAndScoreBySemanticTags(semanticTagShortnames);
    }

    @Override public Map<AnnotationSummary, Float> searchAndScoreBySemanticTags(URI... semanticTags) {
        return _annotatationSummarySearchService.searchAndScoreBySemanticTags(semanticTags);
    }

    @Override protected void doInitialization() throws Exception {
        // do nothing by default
    }

    @Override protected void doTermination() throws Exception {
        // do nothing by default
    }
}
