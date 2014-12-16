package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.fgpt.zooma.exception.QueryCreationException;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.io.HtmlRenderer;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Acts as an endpoint that offers an implementation of the google and freebase suggest API to offer search and
 * autocomplete functionality over zooma identifiable objects.
 * <p/>
 * For more information on the suggest API, see <a href="http://code.google .com/p/google-refine/wiki/SuggestApi">http://code.google.com/p/google-refine/wiki/SuggestApi</a>.
 * Implementations of this class should return matching results using ZOOMA functionality behind the scenes.
 *
 * @param <T> the type of identifiable objects this acts as an endpoint for
 * @param <I> the type of identifier that can be used to obtain unique references to the objects this endpoint returns
 * @author Tony Burdett
 * @date 30/03/12
 */
public abstract class SuggestEndpoint<T, I> {
    private Collection<HtmlRenderer> renderers;
    private Map<Class, Collection<HtmlRenderer>> classRenderersMap;
    private Map<Class, HtmlRenderer> activeClassRendererMap;

    private boolean isValidated = false;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    protected SuggestEndpoint() {
        ArrayList<HtmlRenderer> renderers = new ArrayList<>();
        Map<Class, Collection<HtmlRenderer>> classRenderersMap = new HashMap<>();

        ServiceLoader<HtmlRenderer> htmlRenderers = ServiceLoader.load(HtmlRenderer.class);
        for (HtmlRenderer renderer : htmlRenderers) {
            // add the renderer to the collection
            renderers.add(renderer);
            // add the renderer to the map by rendering class
            if (!classRenderersMap.containsKey(renderer.getRenderingType())) {
                classRenderersMap.put(renderer.getRenderingType(), new ArrayList<HtmlRenderer>());
            }
            classRenderersMap.get(renderer.getRenderingType()).add(renderer);
        }

        this.renderers = Collections.unmodifiableList(renderers);
        this.classRenderersMap = Collections.unmodifiableMap(classRenderersMap);
        this.activeClassRendererMap = Collections.synchronizedMap(new HashMap<Class, HtmlRenderer>());
    }

    public Collection<HtmlRenderer> getHtmlRenderers() {
        return renderers;
    }

    public Collection<HtmlRenderer> getAvailableHtmlRenderers(Class renderingType) {
        return classRenderersMap.get(renderingType);
    }

    public HtmlRenderer getActiveHtmlRenderer(Class renderingType) {
        return activeClassRendererMap.get(renderingType);
    }

    /**
     * Converts a single element into a flyout response, which is essentially a wrapper around some html that gives a
     * summary view of the given entity.
     *
     * @param element the elements to convert
     * @return the flyout response containing some summary html
     */
    protected FlyoutResponse convertToFlyoutResponse(T element) {
        if (element != null) {
            Class renderingClass = element.getClass();

            if (getActiveHtmlRenderer(renderingClass) != null) {
                HtmlRenderer renderer = getActiveHtmlRenderer(renderingClass);
                if (renderer.canRender(element)) {
                    //noinspection unchecked
                    return new SimpleFlyoutResponse(extractElementID(element), renderer.renderHTML(element));
                }
                else {
                    getLog().error("Unexpected type-checking failure: key-class for " + renderer + " was incorrect, " +
                                           "this renderer cannot render objects of type " +
                                           renderingClass.getSimpleName());
                    throw new RuntimeException(
                            "Unexpected type-checking failure: key-class for " + renderer + " was incorrect, " +
                                    "this renderer cannot render objects of type " + renderingClass.getSimpleName());
                }
            }
            else {
                // no active renderer for this class, iterate over all renderers to find one
                for (HtmlRenderer renderer : getHtmlRenderers()) {
                    if (renderer.canRender(element)) {
                        // use this as the active renderer from now on, so we don't have to iterate again
                        activeClassRendererMap.put(renderingClass, renderer);
                        //noinspection unchecked
                        return new SimpleFlyoutResponse(extractElementID(element), renderer.renderHTML(element));
                    }
                }
            }

            // if we got to here, there is no renderer available
            getLog().warn(
                    "No HtmlRenderer available for rendering elements of class '" + renderingClass.getSimpleName() +
                            "'");
            return new SimpleFlyoutResponse(extractElementID(element),
                                            "<div class=\"fbs-flyout-content\">No preview available</div>");
        }
        else {
            // supplied element was null, something went wrong so throw null pointer
            throw new NullPointerException("Failed to generate response: cannot convert null element");
        }
    }

    /**
     * Converts a collection of elements into a search response
     *
     * @param prefix   the prefix that was searched for
     * @param elements the elements to convert
     * @return the search response
     */
    protected SearchResponse convertToSearchResponse(String prefix, Collection<T> elements) {
        SimpleSearchResponse response = new SimpleSearchResponse(prefix);
        for (T t : elements) {
            SimpleSearchResult r = new SimpleSearchResult(extractElementID(t),
                                                          extractElementName(t),
                                                          extractElementTypeID(t),
                                                          extractElementTypeName(t));
            response.addResult(r);
        }
        return response;
    }

    /**
     * Converts a collection of elements into a search response
     *
     * @param prefix         the prefix that was searched for
     * @param scoredElements the elements to convert, mapped to their score
     * @return the search response
     */
    protected SearchResponse convertToSearchResponse(String prefix, Map<T, Float> scoredElements, Sorter<T> sorter) {
        SimpleSearchResponse response = new SimpleSearchResponse(prefix);

        // sort elements
        List<T> elements = sorter.sort(scoredElements);
        // build response
        for (T t : elements) {
            SimpleSearchResult r = new SimpleSearchResult(extractElementID(t),
                                                          extractElementName(t),
                                                          extractElementTypeID(t),
                                                          extractElementTypeName(t),
                                                          scoredElements.get(t).toString());
            response.addResult(r);
        }
        return response;
    }

    /**
     * Converts a collection of elements into a suggest response
     *
     * @param prefix   the prefix that was searched for
     * @param elements the elements to convert
     * @return the suggest response
     */
    protected SuggestResponse convertToSuggestResponse(String prefix, Collection<T> elements) {
        SimpleSuggestedResponse response = new SimpleSuggestedResponse(prefix);
        for (T t : elements) {
            SimpleSuggestResult r = new SimpleSuggestResult(extractElementID(t),
                                                            extractElementName(t),
                                                            extractElementTypeID(t),
                                                            extractElementTypeName(t));
            response.addResult(r);
        }
        return response;
    }

    /**
     * Converts a collection of elements into a suggest response
     *
     * @param prefix         the prefix that was searched for
     * @param scoredElements the elements to convert, mapped to their score
     * @return the suggest response
     */
    protected SuggestResponse convertToSuggestResponse(String prefix, Map<T, Float> scoredElements, Sorter<T> sorter) {
        SimpleSuggestedResponse response = new SimpleSuggestedResponse(prefix);

        // sort elements
        List<T> elements = sorter.sort(scoredElements);
        // build response
        for (T t : elements) {
            SimpleSuggestResult r = new SimpleSuggestResult(extractElementID(t),
                                                            extractElementName(t),
                                                            extractElementTypeID(t),
                                                            extractElementTypeName(t));
            response.addResult(r);
        }
        return response;
    }

    protected void setIsValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    protected boolean requiresValidation() {
        return !isValidated;
    }

    /**
     * Validates that this class has been correctly set up, with all required properties set to non-null values.  This
     * default implementation always returns true, but if implementations add required dependencies they should always
     * check first.
     *
     * @throws IllegalArgumentException if a required property is not set
     */
    protected void validate() throws IllegalArgumentException {
        if (requiresValidation()) {
            setIsValidated(true);
        }
    }

    /**
     * Extracts the URI of an element.  How a URI is assigned for any given object is usually dependant on it's
     * implementation, and therefore cannot be obtained in a generic way.  For each element that is converted by this
     * suggest endpoint, the URI must be extracted and therefore implementations should provide a way to acquire the URI
     * from the concrete type of T for which they implement this endpoint.
     *
     * @param t the entity to extract a name of
     * @return the name of the entity
     */
    protected abstract String extractElementID(T t);

    /**
     * Extracts the name of an element.  How a given object is named is usually dependant on it's implementation, and
     * therefore cannot be obtained in a generic way.  For each element that is converted by this suggest endpoint, the
     * name must be extracted and therefore implementations should provide a way to acquire the name from the concrete
     * type of T for which they implement this endpoint.
     *
     * @param t the entity to extract a name of
     * @return the name of the entity
     */
    protected abstract String extractElementName(T t);

    /**
     * Returns the ID that represents the type of elements this suggest endpoint can deliver.  This will usually be a
     * single string mapped to the generic type this endpoint can return results for.
     *
     * @return a string representing the element type IDs this endpoint can deliver
     */
    protected abstract String extractElementTypeID();

    /**
     * Returns the name that represents the type of elements this suggest endpoint can deliver.  This will usually be a
     * single string mapped to the generic type this endpoint can return results for.
     *
     * @return a string representing the element type namess this endpoint can deliver
     */
    protected abstract String extractElementTypeName();

    /**
     * Returns the ID that represents the type of elements this suggest endpoint can deliver.  Although there is support
     * for individual objects to to return a different type, where you may wish to subclass &gt;T&lt;, this will usually
     * be a single string mapped to the generic type this endpoint can return results for.
     * <p/>
     * You should override this method if you wish for subclassing with more specific types to be supported.
     *
     * @return a string representing the element type IDs this endpoint can deliver
     */
    protected String extractElementTypeID(T t) {
        return extractElementTypeID();
    }

    /**
     * Returns the name that represents the type of elements this suggest endpoint can deliver.  Although there is
     * support for individual objects to to return a different type, where you may wish to subclass &gt;T&lt;, this will
     * usually be a single string mapped to the generic type this endpoint can return results for.
     * <p/>
     * You should override this method if you wish for subclassing with more specific types to be supported.
     *
     * @return a string representing the element type namess this endpoint can deliver
     */
    protected String extractElementTypeName(T t) {
        return extractElementTypeName();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(QueryCreationException.class)
    public @ResponseBody
    String handleException(QueryCreationException e) {
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(SearchException.class)
    private @ResponseBody String handleException(SearchException e) {
        getLog().error("A search exception occurred: " + e.getMessage(), e);
        return "There was a problem performing your search - " + e.getMessage() + "";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    private @ResponseBody String handleException(Exception e) {
        getLog().error("A unexpected exception occurred: " + e.getMessage(), e);
        return "There was a problem performing your search - " + e.getMessage() + "";
    }

    /**
     * Renders a summary view of the entity with the given ID, returning a small html fragment that the client can use
     * to display the result in a user friendly way
     *
     * @param id the ID of the entity to display
     * @return a fragment of html
     */
    public abstract FlyoutResponse flyout(I id);

    /**
     * Performs a freebase suggest query
     *
     * @param query       a string the user has typed
     * @param type        optional, a single string, or an array of strings, specifying the types of result e.g. person,
     *                    product, The actual format of each type depends on the service (e.g., "/government/politician"
     *                    as a Freebase type)
     * @param exact       optional, a string, one of "any", "all", "should"
     * @param limit       optional, an integer to specify how many results to return
     * @param start       optional, an integer to specify the first result to return (thus in conjunction with limit,
     *                    support pagination)
     * @param prefixed    Whether or not to match by name prefix. (used for autosuggest)
     * @param lang        The language you are searching in. Can pass multiple languages.
     * @param domain      A comma separated list of domain IDs. Search results must include these domains.
     * @param filter      A filter s-expression.
     * @param html_escape Whether or not to escape entities.
     * @param indent      Whether to indent the json.
     * @param mql_output  A MQL query thats extracts entity information.
     * @return a response that encapsulates the suggest results
     */
    public abstract SearchResponse search(String query,
                                          String type,
                                          Boolean exact,
                                          Integer limit,
                                          Integer start,
                                          Boolean prefixed,
                                          String lang,
                                          String domain,
                                          String filter,
                                          Boolean html_escape,
                                          Boolean indent,
                                          String mql_output);

    /**
     * Performs a google refine-like suggest query
     *
     * @param prefix      a string the user has typed
     * @param type        optional, a single string, or an array of strings, specifying the types of result e.g. person,
     *                    product, The actual format of each type depends on the service (e.g., "/government/politician"
     *                    as a Freebase type)
     * @param type_strict optional, a string, one of "any", "all", "should"
     * @param limit       optional, an integer to specify how many results to return
     * @param start       optional, an integer to specify the first result to return (thus in conjunction with limit,
     *                    support pagination)
     * @return a response that encapsulates the suggest results
     */
    public abstract SuggestResponse suggest(String prefix,
                                            String type,
                                            String type_strict,
                                            Integer limit,
                                            Integer start);

    private class SimpleSuggestedResponse implements SuggestResponse {
        private static final String OK_RESPONSE = "/api/status/ok";
        private static final String ERROR_RESPONSE = "/api/status/error";

        private String code;
        private String status;
        private String prefix;
        private List<Result> results;

        public SimpleSuggestedResponse(String prefix) {
            this.code = "200 OK";
            this.status = OK_RESPONSE;
            this.prefix = prefix;
            this.results = new ArrayList<Result>();
        }

        @Override public String getCode() {
            return code;
        }

        public void updateCode(String code) {
            this.code = code;
        }

        @Override public String getStatus() {
            return status;
        }

        public void flagErrorStatus() {
            this.status = ERROR_RESPONSE;
        }

        @Override public String getPrefix() {
            return prefix;
        }

        @Override public Result[] getResults() {
            return results.toArray(new Result[results.size()]);
        }

        public void addResult(Result result) {
            results.add(result);
        }
    }

    private class SimpleSuggestResult implements SuggestResponse.Result {
        private String id;
        private String name;
        private NType nType;

        public SimpleSuggestResult(String id, String name, String typeId, String typeName) {
            this.id = id;
            this.name = name;
            this.nType = new SimpleSuggestNType(typeId, typeName);
        }

        @Override public String getId() {
            return id;
        }

        @Override public String getName() {
            return name;
        }

        @Override public NType getNType() {
            return nType;
        }
    }

    private class SimpleSuggestNType implements SuggestResponse.Result.NType {
        private String id;
        private String name;

        private SimpleSuggestNType(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override public String getId() {
            return id;
        }

        @Override public String getName() {
            return name;
        }
    }

    private class SimpleSearchResponse implements SearchResponse {
        private static final String OK_RESPONSE = "/api/status/ok";
        private static final String ERROR_RESPONSE = "/api/status/error";

        private String status;
        private List<Result> results;

        public SimpleSearchResponse(String prefix) {
            this.status = OK_RESPONSE;
            this.results = new ArrayList<>();
        }

        @Override public String getStatus() {
            return status;
        }

        public void flagErrorStatus() {
            this.status = ERROR_RESPONSE;
        }

        @Override public Result[] getResult() {
            return results.toArray(new Result[results.size()]);
        }

        public void addResult(Result result) {
            results.add(result);
        }
    }

    private class SimpleSearchResult implements SearchResponse.Result {
        private String mid;
        private String name;
        private String score;
        private Notable notable;

        public SimpleSearchResult(String mid, String name, String typeId, String typeName) {
            this(mid, name, typeId, typeName, "50");
        }

        public SimpleSearchResult(String mid, String name, String typeId, String typeName, String score) {
            this.mid = mid;
            this.name = name;
            this.score = score;
            this.notable = new SimpleSearchNotable(typeId, typeName);
        }

        @Override public String getMid() {
            return mid;
        }

        @Override public String getName() {
            return name;
        }

        @Override public String getScore() {
            return score;
        }

        @Override public Notable getNotable() {
            return notable;
        }
    }

    private class SimpleSearchNotable implements SearchResponse.Result.Notable {
        private String id;
        private String name;

        private SimpleSearchNotable(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override public String getId() {
            return id;
        }

        @Override public String getName() {
            return name;
        }
    }

    private class SimpleFlyoutResponse implements FlyoutResponse {
        private String id;
        private String html;

        private SimpleFlyoutResponse(String id, String html) {
            this.id = id;
            this.html = html;
        }

        @Override public String getId() {
            return id;
        }

        @Override public String getHtml() {
            return html;
        }
    }
}
