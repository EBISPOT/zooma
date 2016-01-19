package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSummarySearchService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSummaryService;
import uk.ac.ebi.fgpt.zooma.util.InferredAnnotationSummaryCache;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Scorer;
import uk.ac.ebi.fgpt.zooma.util.Sorter;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search ZOOMA for all the unique combinations of mapping between the given property values (and optional types) and
 * described entities given by URI.
 * <p/>
 * This class is a high level convenience implementation for searching annotation summaries.  It will work out of the
 * box, but requires configuration with underlying service implementations. It is also a controller 'stereotype' that
 * can be used to construct a REST API and offers an implementation of the google and freebase suggest API to offer
 * search and autocomplete functionality over ZOOMA annotation summaries.
 * <p/>
 * For more information on the reconcilliation API, see <a href="http://code.google.com/p/google-refine/wiki/ReconciliationServiceApi">
 * http://code.google.com/p/google-refine/wiki/ReconciliationServiceApi </a>. This controller returns matching results
 * using ZOOMA functionality behind the scenes.
 *
 * @author Tony Burdett
 * @date 24/05/12
 */
@Controller
@RequestMapping("/summaries")
public class ZoomaAnnotationSummaries {
    private AnnotationSummaryService annotationSummaryService;

    private AnnotationSummarySearchService annotationSummarySearchService;

    private Sorter<AnnotationSummary> annotationSummarySorter;
    private Limiter<AnnotationSummary> annotationSummaryLimiter;
    private Scorer<AnnotationSummary> annotationSummaryScorer;

    private InferredAnnotationSummaryCache inferredAnnotationSummaryCache = new InferredAnnotationSummaryCache();

    @Autowired
    public ZoomaAnnotationSummaries(
            AnnotationSummaryService annotationSummaryService,
            @Qualifier("annotationSummarySearchService") AnnotationSummarySearchService annotationSummarySearchService,
            Sorter<AnnotationSummary> annotationSummarySorter,
            Limiter<AnnotationSummary> annotationSummaryLimiter,
            Scorer<AnnotationSummary> annotationSummaryScorer) {
        this.annotationSummaryService = annotationSummaryService;
        this.annotationSummarySearchService = annotationSummarySearchService;
        this.annotationSummarySorter = annotationSummarySorter;
        this.annotationSummaryLimiter = annotationSummaryLimiter;
        this.annotationSummaryScorer = annotationSummaryScorer;
    }

    public AnnotationSummaryService getAnnotationSummaryService() {
        return annotationSummaryService;
    }

    public AnnotationSummarySearchService getAnnotationSummarySearchService() {
        return annotationSummarySearchService;
    }

    public Sorter<AnnotationSummary> getAnnotationSummarySorter() {
        return annotationSummarySorter;
    }

    public Limiter<AnnotationSummary> getAnnotationSummaryLimiter() {
        return annotationSummaryLimiter;
    }

    public Scorer<AnnotationSummary> getAnnotationSummaryScorer() {
        return annotationSummaryScorer;
    }

    public Collection<AnnotationSummary> fetch() {
        return fetch(100, 0, null);
    }

   @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<AnnotationSummary> fetch(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "query", required = false) String query) {
        if(query!=null){
            return getAnnotationSummarySearchService().search(query);

        }else {
            if (start == null) {
                if (limit == null) {
                    return getAnnotationSummaryService().getAnnotationSummaries(100, 0);
                } else {
                    return getAnnotationSummaryService().getAnnotationSummaries(limit, 0);
                }
            } else {
                if (limit == null) {
                    return getAnnotationSummaryService().getAnnotationSummaries(100, start);
                } else {
                    return getAnnotationSummaryService().getAnnotationSummaries(limit, start);
                }
            }
        }
    }

    public Collection<AnnotationSummary> queryBySemanticTags(String... semanticTagShortnames) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTagShortnames);
    }

    public Collection<AnnotationSummary> queryBySemanticTags(URI... semanticTags) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTags);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query) {
        return queryAndScore(query, false);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, boolean prefixed) {
        Collection<AnnotationSummary> annotations = prefixed
                ? getAnnotationSummarySearchService().searchByPrefix(query)
                : getAnnotationSummarySearchService().search(query);
        return getAnnotationSummaryScorer().score(annotations, query);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, String type) {
        return queryAndScore(query, type, false);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, String type, boolean prefixed) {
        Collection<AnnotationSummary> annotations = prefixed
                ? getAnnotationSummarySearchService().searchByPrefix(type, query)
                : getAnnotationSummarySearchService().search(type, query);
        return getAnnotationSummaryScorer().score(annotations, query, type);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query,
                                                       String type,
                                                       List<URI> preferredSources,
                                                       URI[] requiredSources) {
        Collection<AnnotationSummary> annotations =
                getAnnotationSummarySearchService().searchByPreferredSources(type,
                                                                             query,
                                                                             preferredSources,
                                                                             requiredSources);
        return getAnnotationSummaryScorer().score(annotations, query, type);
    }

    public Map<AnnotationSummary, Float> queryAndScoreBySemanticTags(String... semanticTagShortnames) {
        Collection<AnnotationSummary> annotations =
                getAnnotationSummarySearchService().searchBySemanticTags(semanticTagShortnames);
        return getAnnotationSummaryScorer().score(annotations);
    }

    public Map<AnnotationSummary, Float> queryAndScoreBySemanticTags(URI... semanticTags) {
        Collection<AnnotationSummary> annotations =
                getAnnotationSummarySearchService().searchBySemanticTags(semanticTags);
        return getAnnotationSummaryScorer().score(annotations);
    }
}
