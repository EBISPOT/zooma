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
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.Collection;
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
public class ZoomaAnnotationSummarySearcher extends SuggestEndpoint<AnnotationSummary, String> {
    private AnnotationSummaryService annotationSummaryService;

    //annotationSummarySearchService including new improvements 
    private AnnotationSummarySearchService annotationSummarySearchService;

    //annotationSummarySearchService without improvements 
    private AnnotationSummarySearchService annotationSummarySearchServiceOLD;


    private Sorter<AnnotationSummary> annotationSummarySorter;
    private Limiter<AnnotationSummary> annotationSummaryLimiter;


    public AnnotationSummaryService getAnnotationSummaryService() {
        return annotationSummaryService;
    }

    @Autowired
    public void setAnnotationSummaryService(AnnotationSummaryService annotationSummaryService) {
        this.annotationSummaryService = annotationSummaryService;
    }

    public AnnotationSummarySearchService getAnnotationSummarySearchService() {
        return annotationSummarySearchService;
    }

    @Autowired
    @Qualifier("annotationSummarySearchService")
    public void setAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        this.annotationSummarySearchService = annotationSummarySearchService;
    }

    @Autowired
    @Qualifier("annotationSummarySearchService")
    public void setAnnotationSummarySearchServiceOLD(AnnotationSummarySearchService annotationSummarySearchService) {
        this.annotationSummarySearchServiceOLD = annotationSummarySearchService;
    }

    public AnnotationSummarySearchService getAnnotationSummarySearchServiceOLD() {
        return annotationSummarySearchServiceOLD;
    }

    public Sorter<AnnotationSummary> getAnnotationSummarySorter() {
        return annotationSummarySorter;
    }

    @Autowired
    public void setAnnotationSummarySorter(Sorter<AnnotationSummary> annotationSummarySorter) {
        setIsValidated(false);
        this.annotationSummarySorter = annotationSummarySorter;
    }

    public Limiter<AnnotationSummary> getAnnotationSummaryLimiter() {
        return annotationSummaryLimiter;
    }

    @Autowired
    public void setAnnotationSummaryLimiter(Limiter<AnnotationSummary> annotationSummaryLimiter) {
        setIsValidated(false);
        this.annotationSummaryLimiter = annotationSummaryLimiter;
    }

    public Collection<AnnotationSummary> query(String query, boolean prefixed) {
        getLog().debug("Querying for " + query + " (prefixed = " + prefixed + ")");
        validate();
        Map<AnnotationSummary, Float> allAnnotations = prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(query)
                : getAnnotationSummarySearchService().searchAndScore(query);
        return getAnnotationSummarySorter().sort(allAnnotations);
    }

    public Collection<AnnotationSummary> query(String query, String type, boolean prefixed) {
        getLog().debug("Querying for " + query + ", " + type + " (prefixed = " + prefixed + ")");
        validate();
        Map<AnnotationSummary, Float> allAnnotations = prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchService().searchAndScore(type, query);
        return getAnnotationSummarySorter().sort(allAnnotations, type, query);
    }

    public Collection<AnnotationSummary> query(String query, String type, int limit, int start, boolean prefixed) {
        getLog().debug("Querying for " + query + ", " + type + ", " + limit + ", " + start +
                               " (prefixed = " + prefixed + ")");
        validate();
        Map<AnnotationSummary, Float> allAnnotations = prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchService().searchAndScore(type, query);
        List<AnnotationSummary> allAnnotationsList = getAnnotationSummarySorter().sort(allAnnotations, type, query);
        return getAnnotationSummaryLimiter().limit(allAnnotationsList, limit, start);
    }

    public Collection<AnnotationSummary> queryBySemanticTags(String... semanticTagShortnames) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTagShortnames);
    }

    public Collection<AnnotationSummary> queryBySemanticTags(URI... semanticTags) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTags);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, boolean prefixed) {

        System.out.println("queryAndScore    Querying for " + query + " (prefixed = " + prefixed + ")");
        getLog().debug("queryAndScore    Querying for " + query + " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(query)
                : getAnnotationSummarySearchService().searchAndScore_QueryExpansion(query);
    }

    public Map<AnnotationSummary, Float> queryAndScoreOLD(String query, boolean prefixed) {

        System.out.println("queryAndScoreOLD    Querying for " + query + " (prefixed = " + prefixed + ")");
        getLog().debug("queryAndScoreOLD    Querying for " + query + " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchServiceOLD().searchAndScoreByPrefix(query)
                : getAnnotationSummarySearchServiceOLD().searchAndScore(query);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, String type, boolean prefixed) {
        System.out.println("queryAndScore    Querying for " + query + " (prefixed = " + prefixed + ")");

        getLog().debug("queryAndScore    Querying for " + query + ", " + type + " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchService().searchAndScore_QueryExpansion(type, query);

    }

    public Map<AnnotationSummary, Float> queryAndScoreOLD(String query, String type, boolean prefixed) {

        System.out.println("queryAndScoreOLD    Querying for " + query + " (prefixed = " + prefixed + ")");


        getLog().debug("queryAndScoreOLD    Querying for " + query + ", " + type + " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchServiceOLD().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchServiceOLD().searchAndScore(type, query);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query,
                                                       String type,
                                                       boolean prefixed,
                                                       int limit,
                                                       int start) {

        System.out.println("queryAndScore    Querying for " + query + " (prefixed = " + prefixed + ")");

        getLog().debug("queryAndScore    Querying for " + query + ", " + type + ", " + limit + ", " + start +
                               " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchService().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchService().searchAndScore_QueryExpansion(type, query);
    }

    public Map<AnnotationSummary, Float> queryAndScoreOLD(String query,
                                                          String type,
                                                          boolean prefixed,
                                                          int limit,
                                                          int start) {

        System.out.println("queryAndScoreOLD    Querying for " + query + " (prefixed = " + prefixed + ")");

        getLog().debug("queryAndScoreOLD    Querying for " + query + ", " + type + ", " + limit + ", " + start +
                               " (prefixed = " + prefixed + ")");
        validate();
        return prefixed
                ? getAnnotationSummarySearchServiceOLD().searchAndScoreByPrefix(type, query)
                : getAnnotationSummarySearchServiceOLD().searchAndScore(type, query);
    }

    public Map<AnnotationSummary, Float> queryAndScoreBySemanticTags(String... semanticTagShortnames) {
        return getAnnotationSummarySearchService().searchAndScoreBySemanticTags(semanticTagShortnames);
    }

    public Map<AnnotationSummary, Float> queryAndScoreBySemanticTags(URI... semanticTags) {
        return getAnnotationSummarySearchService().searchAndScoreBySemanticTags(semanticTags);
    }

    @Override protected String extractElementID(AnnotationSummary annotationSummary) {
        return annotationSummary.getID();
    }

    @Override protected String extractElementName(AnnotationSummary annotationSummary) {
        return annotationSummary.getAnnotatedPropertyValue();
    }

    protected String extractElementTypeID(AnnotationSummary annotationSummary) {
        return annotationSummary.getAnnotationSummaryTypeID();
    }

    protected String extractElementTypeName(AnnotationSummary annotationSummary) {
        return annotationSummary.getAnnotationSummaryTypeName();
    }

    @Override protected String extractElementTypeID() {
        return AnnotationSummary.ANNOTATION_SUMMARY_TYPE_ID;
    }

    @Override protected String extractElementTypeName() {
        return AnnotationSummary.ANNOTATION_SUMMARY_TYPE_NAME;
    }

    @Override
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    public @ResponseBody SuggestResponse suggest(
            @RequestParam(value = "prefix") String prefix,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "type_strict", required = false) String type_strict,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start) {

        //System.out.println("****suggest");

        getLog().debug("****suggest");

        // check each non-required argument and defer to appropriate query form
        // NB. we don't use type_strict param anywhere
        if (type != null) {
            if (limit != null) {
                if (start != null) {
                    return convertToSuggestResponse(prefix,
                                                    queryAndScore(prefix, type, true, limit, start),
                                                    getAnnotationSummarySorter());
                }
                else {
                    return convertToSuggestResponse(prefix,
                                                    queryAndScore(prefix, type, true, limit, 0),
                                                    getAnnotationSummarySorter());
                }
            }
            else {
                return convertToSuggestResponse(prefix,
                                                queryAndScore(prefix, type, true),
                                                getAnnotationSummarySorter());
            }
        }
        else {
            return convertToSuggestResponse(prefix, queryAndScore(prefix, true), getAnnotationSummarySorter());
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody SearchResponse search(
            @RequestParam(value = "query", required = false) final String query,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "exact", required = false, defaultValue = "false") final Boolean exact,
            @RequestParam(value = "limit", required = false, defaultValue = "20") final Integer limit,
            @RequestParam(value = "start", required = false, defaultValue = "0") final Integer start,
            @RequestParam(value = "prefixed", required = false, defaultValue = "false") final Boolean prefixed,
            @RequestParam(value = "lang", required = false) final String lang,
            @RequestParam(value = "domain", required = false) final String domain,
            @RequestParam(value = "filter", required = false) final String filter,
            @RequestParam(value = "html_escape", required = false, defaultValue = "true") final Boolean html_escape,
            @RequestParam(value = "indent", required = false, defaultValue = "false") final Boolean indent,
            @RequestParam(value = "mql_output", required = false) final String mql_output,
            @RequestParam(value = "semanticTag", required = false) String[] semanticTags) {
        validateArguments(query, type, exact, limit, start, prefixed, semanticTags);

        getLog().debug("****search RequestMapping");
        System.out.println("****search RequestMapping");

        if (semanticTags != null) {
            return searchBySemanticTags(semanticTags);
        }
        else {
            getLog().debug("****calling search()");

            return search(query,
                          type,
                          exact,
                          limit,
                          start,
                          prefixed,
                          lang,
                          domain,
                          filter,
                          html_escape,
                          indent,
                          mql_output);
        }
    }


    @RequestMapping(value = "/searchOLD", method = RequestMethod.GET)
    public @ResponseBody SearchResponse searchOLD(
            @RequestParam(value = "query", required = false) final String query,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "exact", required = false, defaultValue = "false") final Boolean exact,
            @RequestParam(value = "limit", required = false, defaultValue = "20") final Integer limit,
            @RequestParam(value = "start", required = false, defaultValue = "0") final Integer start,
            @RequestParam(value = "prefixed", required = false, defaultValue = "false") final Boolean prefixed,
            @RequestParam(value = "lang", required = false) final String lang,
            @RequestParam(value = "domain", required = false) final String domain,
            @RequestParam(value = "filter", required = false) final String filter,
            @RequestParam(value = "html_escape", required = false, defaultValue = "true") final Boolean html_escape,
            @RequestParam(value = "indent", required = false, defaultValue = "false") final Boolean indent,
            @RequestParam(value = "mql_output", required = false) final String mql_output,
            @RequestParam(value = "semanticTag", required = false) String[] semanticTags) {
        validateArguments(query, type, exact, limit, start, prefixed, semanticTags);

        getLog().debug("****oldsearch RequestMapping");
        //System.out.println("****oldsearch RequestMapping ");

        if (semanticTags != null) {
            return searchBySemanticTags(semanticTags);
        }
        else {

            getLog().debug("****calling searchOLD()");

            return searchOLD(query,
                             type,
                             exact,
                             limit,
                             start,
                             prefixed,
                             lang,
                             domain,
                             filter,
                             html_escape,
                             indent,
                             mql_output);
        }
    }

    @Override
    public SearchResponse search(final String query,
                                 final String type,
                                 final Boolean exact,
                                 final Integer limit,
                                 final Integer start,
                                 final Boolean prefixed,
                                 final String lang,
                                 final String domain,
                                 final String filter,
                                 final Boolean html_escape,
                                 final Boolean indent,
                                 final String mql_output) {


        getLog().debug("****search noRequestMapping");
        System.out.println("****search noRequestMapping");

        // NB. Limited implementations of freebase functionality so far, we only use query, type and limiting of results
        if (type != null) {
            if (limit != null) {
                if (start != null) {
                    return convertToSearchResponse(query,
                                                   queryAndScore(query, type, prefixed, limit, start),
                                                   getAnnotationSummarySorter());
                }
                else {
                    return convertToSearchResponse(query,
                                                   queryAndScore(query, type, prefixed, limit, 0),
                                                   getAnnotationSummarySorter());
                }
            }
            else {
                return convertToSearchResponse(query,
                                               queryAndScore(query, type, prefixed),
                                               getAnnotationSummarySorter());
            }
        }
        else {
            return convertToSearchResponse(query, queryAndScore(query, prefixed), getAnnotationSummarySorter());
        }
    }


    public SearchResponse searchOLD(final String query,
                                    final String type,
                                    final Boolean exact,
                                    final Integer limit,
                                    final Integer start,
                                    final Boolean prefixed,
                                    final String lang,
                                    final String domain,
                                    final String filter,
                                    final Boolean html_escape,
                                    final Boolean indent,
                                    final String mql_output) {

        getLog().debug("****searchOLD norequest");
        //System.out.println("****searchOLD");

        // NB. Limited implementations of freebase functionality so far, we only use query, type and limiting of results
        if (type != null) {
            if (limit != null) {
                if (start != null) {
                    return convertToSearchResponse(query,
                                                   queryAndScoreOLD(query, type, prefixed, limit, start),
                                                   getAnnotationSummarySorter());
                }
                else {
                    return convertToSearchResponse(query,
                                                   queryAndScoreOLD(query, type, prefixed, limit, 0),
                                                   getAnnotationSummarySorter());
                }
            }
            else {
                return convertToSearchResponse(query,
                                               queryAndScoreOLD(query, type, prefixed),
                                               getAnnotationSummarySorter());
            }
        }
        else {
            return convertToSearchResponse(query, queryAndScoreOLD(query, prefixed), getAnnotationSummarySorter());
        }
    }


    @Override
    @RequestMapping(value = "/flyout", method = RequestMethod.GET)
    public @ResponseBody FlyoutResponse flyout(@RequestParam(value = "id") final String id) {
        return convertToFlyoutResponse(fetch(id));
    }

    @RequestMapping(value = "/{annotationSummaryID}", method = RequestMethod.GET)
    public @ResponseBody AnnotationSummary fetch(@PathVariable String annotationSummaryID) {
        return getAnnotationSummaryService().getAnnotationSummary(annotationSummaryID);
    }

    /**
     * Validates that this class has been correctly set up, with all required properties set to non-null values.
     *
     * @throws IllegalArgumentException if a required property is not set
     */
    protected void validate() throws IllegalArgumentException {
        if (requiresValidation()) {
            Assert.notNull(getAnnotationSummarySearchService(), "Annotation summary service must not be null");
            Assert.notNull(getAnnotationSummarySorter(), "Annotation summary sorter must not be null");
            Assert.notNull(getAnnotationSummaryLimiter(), "Annotation summary limiter must not be null");
            setIsValidated(true);
        }
    }

    protected void validateArguments(String query,
                                     String type,
                                     Boolean exact,
                                     Integer limit,
                                     Integer start,
                                     Boolean prefixed,
                                     String[] semanticTags)
            throws IllegalArgumentException {
        // test that we haven't been given query/type AND semantic tags
        if ((query != null || type != null) && semanticTags != null) {
            throw new IllegalArgumentException(
                    "Please either specify query and/or type, or semantic tag, arguments - " +
                            "querying by both is not supported");
        }
    }

    protected SearchResponse searchBySemanticTags(String[] semanticTags) {
        return convertToSearchResponse(semanticTags.length + " semantic tags", queryBySemanticTags(semanticTags));
    }
}
