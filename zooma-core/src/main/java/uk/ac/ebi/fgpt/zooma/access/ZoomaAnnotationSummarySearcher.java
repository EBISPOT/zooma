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
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSourceService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSummarySearchService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSummaryService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Scorer;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private AnnotationSourceService annotationSourceService;

    private AnnotationSummarySearchService annotationSummarySearchService;

    private Sorter<AnnotationSummary> annotationSummarySorter;
    private Limiter<AnnotationSummary> annotationSummaryLimiter;
    private Scorer<AnnotationSummary> annotationSummaryScorer;

    public AnnotationSummaryService getAnnotationSummaryService() {
        return annotationSummaryService;
    }

    @Autowired
    public void setAnnotationSummaryService(AnnotationSummaryService annotationSummaryService) {
        this.annotationSummaryService = annotationSummaryService;
    }

    public AnnotationSourceService getAnnotationSourceService() {
        return annotationSourceService;
    }

    @Autowired
    public void setAnnotationSourceService(AnnotationSourceService annotationSourceService) {
        this.annotationSourceService = annotationSourceService;
    }

    public AnnotationSummarySearchService getAnnotationSummarySearchService() {
        return annotationSummarySearchService;
    }

    @Autowired
    @Qualifier("annotationSummarySearchService")
    public void setAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        this.annotationSummarySearchService = annotationSummarySearchService;
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

    public Scorer<AnnotationSummary> getAnnotationSummaryScorer() {
        return annotationSummaryScorer;
    }

    @Autowired
    public void setAnnotationSummaryScorer(Scorer<AnnotationSummary> annotationSummaryScorer) {
        setIsValidated(false);
        this.annotationSummaryScorer = annotationSummaryScorer;
    }

    public Collection<AnnotationSummary> queryBySemanticTags(String... semanticTagShortnames) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTagShortnames);
    }

    public Collection<AnnotationSummary> queryBySemanticTags(URI... semanticTags) {
        return getAnnotationSummarySearchService().searchBySemanticTags(semanticTags);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, boolean prefixed) {
        validate();
        Collection<AnnotationSummary> annotations = prefixed
                ? getAnnotationSummarySearchService().searchByPrefix(query)
                : getAnnotationSummarySearchService().search(query);
        return getAnnotationSummaryScorer().score(annotations, query);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query, String type, boolean prefixed) {
        validate();
        Collection<AnnotationSummary> annotations = prefixed
                ? getAnnotationSummarySearchService().searchByPrefix(type, query)
                : getAnnotationSummarySearchService().search(type, query);
        return getAnnotationSummaryScorer().score(annotations, query, type);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query,
                                                       String type,
                                                       boolean prefixed,
                                                       URI[] requiredSources) {
        validate();
        Collection<AnnotationSummary> annotations = prefixed
                ? getAnnotationSummarySearchService().searchByPrefix(type, query, requiredSources)
                : getAnnotationSummarySearchService().search(type, query, requiredSources);
        return getAnnotationSummaryScorer().score(annotations, query, type);
    }

    public Map<AnnotationSummary, Float> queryAndScore(String query,
                                                       String type,
                                                       List<URI> preferredSources,
                                                       URI[] requiredSources) {
        validate();
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
        // check each non-required argument and defer to appropriate query form
        // NB. we don't use type_strict param anywhere
        if (type != null) {
            return convertToSuggestResponse(prefix,
                                            queryAndScore(prefix, type, true),
                                            getAnnotationSummarySorter());
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
        validateArguments(query, type, semanticTags);
        if (semanticTags != null) {
            return searchBySemanticTags(semanticTags);
        }
        else {
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
        if (filter != null) {
            return filteredSearch(query,
                                  type,
                                  prefixed,
                                  filter);
        }
        else {
            return unfilteredSearch(query,
                                    type,
                                    prefixed);
        }
    }

    protected SearchResponse unfilteredSearch(final String query,
                                              final String type,
                                              final Boolean prefixed) {
        // NB. Limited implementations of freebase functionality so far, we only use query, type and limiting of results
        if (type != null) {
            return convertToSearchResponse(query,
                                           queryAndScore(query, type, prefixed),
                                           getAnnotationSummarySorter());
        }
        else {
            return convertToSearchResponse(query, queryAndScore(query, prefixed), getAnnotationSummarySorter());
        }
    }

    protected SearchResponse filteredSearch(final String query,
                                            final String type,
                                            final Boolean prefixed,
                                            final String filter) {
        SearchType searchType = validateFilterArguments(filter);
        URI[] requiredSources = new URI[0];
        List<URI> preferredSources;
        switch (searchType) {
            case REQUIRED_ONLY:
                requiredSources = parseRequiredSourcesFromFilter(filter);
                return convertToSearchResponse(query,
                                               queryAndScore(query, type, prefixed, requiredSources),
                                               getAnnotationSummarySorter());
            case REQUIRED_AND_PREFERRED:
                requiredSources = parseRequiredSourcesFromFilter(filter);
            case PREFERRED_ONLY:
                preferredSources = parsePreferredSourcesFromFilter(filter);
                return convertToSearchResponse(query,
                                               queryAndScore(query, type, preferredSources, requiredSources),
                                               getAnnotationSummarySorter());
            case UNRESTRICTED:
            default:
                return unfilteredSearch(query,
                                        type,
                                        prefixed);
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
            Assert.notNull(getAnnotationSummaryScorer(), "Annotation summary scorer must not be null");
            setIsValidated(true);
        }
    }

    /**
     * Validates that a legal combination of arguments has been passed to the search method by running some checks for
     * conflicting search modes for the given set of parameters and returns an informative error code if the supplied
     * set of arguments is invalid
     *
     * @param query        they query string
     * @param type         a typing string
     * @param semanticTags the semantic tags to restrict to
     * @throws IllegalArgumentException the the supplied combination arguments cannot be used together in one search
     */
    protected void validateArguments(String query,
                                     String type,
                                     String[] semanticTags)
            throws IllegalArgumentException {
        // test that we haven't been given query/type AND semantic tags
        if ((query != null || type != null) && semanticTags != null) {
            throw new IllegalArgumentException(
                    "Please either specify query and/or type, or semantic tag, arguments - " +
                            "querying by both is not supported");
        }
    }

    protected SearchType validateFilterArguments(String filter) {
        // filter argument should look like this:
        // &filter=required:[x,y];preferred:[x,y,z]
        // where required or preferred are optional and both arrays can contain any number of elements
        if (filter.contains("required")) {
            if (filter.contains("preferred")) {
                return SearchType.REQUIRED_AND_PREFERRED;
            }
            else {
                return SearchType.REQUIRED_ONLY;
            }
        }
        else {
            if (filter.contains("preferred")) {
                return SearchType.PREFERRED_ONLY;
            }
            else {
                return SearchType.UNRESTRICTED;
            }
        }
    }

    protected URI[] parseRequiredSourcesFromFilter(String filter) {
        Matcher requiredMatcher = Pattern.compile("required:\\[([^\\]]+)\\]").matcher(filter);
        List<URI> requiredSources = new ArrayList<>();
        if (requiredMatcher.find(filter.indexOf("required:"))) {
            String sourceNames = requiredMatcher.group(1);
            String[] tokens = sourceNames.split(",", -1);
            for (String sourceName : tokens) {
                AnnotationSource nextSource = getAnnotationSourceService().getAnnotationSource(sourceName);
                if (nextSource != null) {
                    requiredSources.add(nextSource.getURI());
                }
                else {
                    getLog().warn("Required source '" + sourceName + "' was specified as a filter but " +
                                          "could not be found in ZOOMA; this source will be excluded from the query");
                }
            }
        }

        return requiredSources.toArray(new URI[requiredSources.size()]);
    }

    protected List<URI> parsePreferredSourcesFromFilter(String filter) {
        Matcher requiredMatcher = Pattern.compile("preferred:\\[([^\\]]+)\\]").matcher(filter);
        List<URI> preferredSources = new ArrayList<>();
        if (requiredMatcher.find(filter.indexOf("preferred:"))) {
            String sourceNames = requiredMatcher.group(1);
            String[] tokens = sourceNames.split(",", -1);
            for (String sourceName : tokens) {
                AnnotationSource nextSource = getAnnotationSourceService().getAnnotationSource(sourceName);
                if (nextSource != null) {
                    preferredSources.add(nextSource.getURI());
                }
                else {
                    getLog().warn("Required source '" + sourceName + "' was specified as a filter but " +
                                          "could not be found in ZOOMA; this source will be excluded from the query");
                }
            }
        }
        return preferredSources;
    }

    protected SearchResponse searchBySemanticTags(String[] semanticTags) {
        return convertToSearchResponse(semanticTags.length + " semantic tags", queryBySemanticTags(semanticTags));
    }

    protected enum SearchType {
        REQUIRED_ONLY,
        PREFERRED_ONLY,
        REQUIRED_AND_PREFERRED,
        UNRESTRICTED
    }
}
