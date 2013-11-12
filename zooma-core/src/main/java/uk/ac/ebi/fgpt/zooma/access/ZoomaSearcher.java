package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the standard endpoint for all zooma searches.  This will only delegate searches to a {@link
 * ZoomaAnnotationSummarySearcher} by default, although you can enable wider searches using the supplied flags to
 * "switch on" a {@link ZoomaAnnotationSearcher}, {@link ZoomaPropertySearcher} and {@link
 * ZoomaPropertyTypeSearcher}.
 * <p/>
 * By enabling each search service, the results returned from a search become composited.  The order used when
 * compositing is always {@link uk.ac.ebi.fgpt.zooma.model.AnnotationSummary} &lt; {@link
 * uk.ac.ebi.fgpt.zooma.model.Annotation} &lt; {@link uk.ac.ebi.fgpt.zooma.model.Property} &lt; Property Type.
 * <p/>
 *
 * @author Tony Burdett
 * @date 11/04/12
 */
@Controller
@RequestMapping
public class ZoomaSearcher extends SuggestEndpoint<Object, String> {
    private ZoomaAnnotationSearcher annotationSearchEngine;
    private ZoomaAnnotationSummarySearcher annotationSummarySearchEngine;
    private ZoomaPropertySearcher propertySearchEngine;
    private ZoomaPropertyTypeSearcher propertyTypeSearchEngine;

    private boolean annotationSummarySearchingEnabled;
    private boolean annotationSearchingEnabled;
    private boolean propertySearchingEnabled;
    private boolean propertyTypeSearchingEnabled;

    public ZoomaSearcher() {
        this(true, false, false, false);
    }

    public ZoomaSearcher(boolean annotationSummarySearchingEnabled,
                         boolean annotationSearchingEnabled,
                         boolean propertySearchingEnabled,
                         boolean propertyTypeSearchingEnabled) {
        setAnnotationSummarySearchingEnabled(annotationSummarySearchingEnabled);
        setAnnotationSearchingEnabled(annotationSearchingEnabled);
        setPropertySearchingEnabled(propertySearchingEnabled);
        setPropertyTypeSearchingEnabled(propertyTypeSearchingEnabled);
    }

    public ZoomaAnnotationSummarySearcher getAnnotationSummarySearchEngine() {
        return annotationSummarySearchEngine;
    }

    @Autowired
    public void setAnnotationSummarySearchEngine(ZoomaAnnotationSummarySearcher annotationSummarySearchEngine) {
        this.annotationSummarySearchEngine = annotationSummarySearchEngine;
    }

    public boolean isAnnotationSummarySearchingEnabled() {
        return annotationSummarySearchingEnabled;
    }

    public void setAnnotationSummarySearchingEnabled(boolean annotationSummarySearchingEnabled) {
        this.annotationSummarySearchingEnabled = annotationSummarySearchingEnabled;
    }

    public ZoomaAnnotationSearcher getAnnotationSearchEngine() {
        return annotationSearchEngine;
    }

    @Autowired
    public void setAnnotationSearchEngine(ZoomaAnnotationSearcher annotationSearchEngine) {
        this.annotationSearchEngine = annotationSearchEngine;
    }

    public boolean isAnnotationSearchingEnabled() {
        return annotationSearchingEnabled;
    }

    public void setAnnotationSearchingEnabled(boolean annotationSearchingEnabled) {
        this.annotationSearchingEnabled = annotationSearchingEnabled;
    }

    public ZoomaPropertySearcher getPropertySearchEngine() {
        return propertySearchEngine;
    }

    @Autowired
    public void setPropertySearchEngine(ZoomaPropertySearcher propertySearchEngine) {
        this.propertySearchEngine = propertySearchEngine;
    }

    public boolean isPropertySearchingEnabled() {
        return propertySearchingEnabled;
    }

    public void setPropertySearchingEnabled(boolean propertySearchingEnabled) {
        this.propertySearchingEnabled = propertySearchingEnabled;
    }

    public ZoomaPropertyTypeSearcher getPropertyTypeSearchEngine() {
        return propertyTypeSearchEngine;
    }

    @Autowired
    public void setPropertyTypeSearchEngine(ZoomaPropertyTypeSearcher propertyTypeSearchEngine) {
        this.propertyTypeSearchEngine = propertyTypeSearchEngine;
    }

    public boolean isPropertyTypeSearchingEnabled() {
        return propertyTypeSearchingEnabled;
    }

    public void setPropertyTypeSearchingEnabled(boolean propertyTypeSearchingEnabled) {
        this.propertyTypeSearchingEnabled = propertyTypeSearchingEnabled;
    }

    @Override protected String extractElementID(Object o) {
        throw new UnsupportedOperationException("Cannot determine ID of elements of type '" + o.getClass() + "' " +
                                                        "from composite search engine - use an appropriate " +
                                                        "implementation");
    }

    @Override protected String extractElementName(Object o) {
        throw new UnsupportedOperationException("Cannot determine name of elements of type '" + o.getClass() + "' " +
                                                        "from composite search engine - use an appropriate " +
                                                        "implementation");
    }

    @Override protected String extractElementTypeID() {
        throw new UnsupportedOperationException("Cannot determine type ID of elements from composite search engine - " +
                                                        "use an appropriate implementation");
    }

    @Override protected String extractElementTypeName() {
        throw new UnsupportedOperationException(
                "Cannot determine type name of elements from composite search engine - " +
                        "use an appropriate implementation");
    }

    @Override
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    public @ResponseBody SuggestResponse suggest(
            @RequestParam(value = "prefix") String prefix,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "type_strict", required = false) String type_strict,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start) {
        // delegate requests to each search engine
        List<SuggestResponse> responses = new ArrayList<>();
        if (isAnnotationSummarySearchingEnabled()) {
            responses.add(getAnnotationSummarySearchEngine().suggest(prefix, type, type_strict, limit, start));
        }
        if (isAnnotationSearchingEnabled()) {
            responses.add(getAnnotationSearchEngine().suggest(prefix, type, type_strict, limit, start));
        }
        if (isPropertySearchingEnabled()) {
            responses.add(getPropertySearchEngine().suggest(prefix, type, type_strict, limit, start));
        }
        if (isPropertyTypeSearchingEnabled()) {
            responses.add(getPropertyTypeSearchEngine().suggest(prefix, type, type_strict, limit, start));
        }

        // and aggregate results
        return aggregateSuggestResponses(responses.toArray(new SuggestResponse[responses.size()]));
    }

    public SearchResponse search(final String query) {
        return search(query, null, null, null, null, null, null, null, null, null, null, null);
    }

    public SearchResponse search(final String query, final String type) {
        return search(query, type, null, null, null, null, null, null, null, null, null, null);
    }

    public SearchResponse search(final String query, final Integer limit, final Integer start) {
        return search(query, null, null, limit, start, null, null, null, null, null, null, null);
    }

    public SearchResponse search(final String query, final String type, final Integer limit, final Integer start) {
        return search(query, type, null, limit, start, null, null, null, null, null, null, null);
    }

    @Override
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody SearchResponse search(
            @RequestParam(value = "query") final String query,
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
            @RequestParam(value = "mql_output", required = false) final String mql_output) {
        getLog().debug("ZOOMA search engine received search request (query '" + query + "')");
        
        getLog().debug("Zooma Searcher... ZOOMA search engine received search request (query '" + query + "')");
        System.out.println("Zooma Searcher...");
        try {
            // delegate requests to each search engine
            List<SearchResponse> responses = new ArrayList<>();
            if (isAnnotationSummarySearchingEnabled()) {
                responses.add(getAnnotationSummarySearchEngine().search(query,
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
                                                                        mql_output));
            }
            if (isAnnotationSearchingEnabled()) {
                responses.add(getAnnotationSearchEngine().search(query,
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
                                                                 mql_output));
            }
            if (isPropertySearchingEnabled()) {
                responses.add(getPropertySearchEngine().search(query,
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
                                                               mql_output));
            }
            if (isPropertyTypeSearchingEnabled()) {
                responses.add(getPropertyTypeSearchEngine().search(query,
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
                                                                   mql_output));
            }

            // and aggregate results
            getLog().debug("Acquired individual search responses, combining results into formulated response.");
            return aggregateSearchResponses(responses.toArray(new SearchResponse[responses.size()]));
        }
        catch (Exception e) {
            getLog().error("Unexpected error", e);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    @RequestMapping(value = "/flyout", method = RequestMethod.GET)
    public @ResponseBody FlyoutResponse flyout(@RequestParam(value = "id") final String id) {
        // delegate requests to each search engine in turn - catch NPE if no available flyout response
        getLog().debug("ZOOMA search engine received flyout request (id '" + id + "')");
        try {
            return getAnnotationSummarySearchEngine().flyout(id);
        }
        catch (NullPointerException npe) {
            try {
                return getAnnotationSearchEngine().flyout(URI.create(id));
            }
            catch (NullPointerException npe2) {
                try {
                    return getPropertySearchEngine().flyout(URI.create(id));
                }
                catch (NullPointerException npe3) {
                    try {
                        return getPropertyTypeSearchEngine().flyout(id);
                    }
                    catch (NullPointerException npe4) {
                        getLog().error("Null flyout response for entity with ID '" + id + "'");
                        throw npe3;
                    }
                }
            }
        }
    }

    private SearchResponse aggregateSearchResponses(final SearchResponse... responses) {
        return new SearchResponse() {
            @Override public String getStatus() {
                return "/api/status/ok";
            }

            @Override public Result[] getResult() {
                List<Result> results = new ArrayList<>();
                for (SearchResponse response : responses) {
                    if (response != null) {
                        Collections.addAll(results, response.getResult());
                    }
                }
                return results.toArray(new Result[results.size()]);
            }
        };
    }

    private SuggestResponse aggregateSuggestResponses(final SuggestResponse... responses) {
        return new SuggestResponse() {
            @Override public String getCode() {
                return "200 OK";
            }

            @Override public String getStatus() {
                return "/api/status/ok";
            }

            @Override public String getPrefix() {
                return responses[0].getPrefix();
            }

            @Override public Result[] getResults() {
                List<Result> results = new ArrayList<>();
                for (SuggestResponse response : responses) {
                    if (response != null) {
                        Collections.addAll(results, response.getResults());
                    }
                }
                return results.toArray(new Result[results.size()]);
            }
        };
    }
}
