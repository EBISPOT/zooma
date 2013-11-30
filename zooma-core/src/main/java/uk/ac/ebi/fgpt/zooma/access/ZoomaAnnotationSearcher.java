package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSearchService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Search ZOOMA for {@link uk.ac.ebi.fgpt.zooma.model.Annotation}s matching the supplied property value prefix and,
 * optionally, types.
 * <p/>
 * This class is a high level convenience implementation for searching annotations.  It will work out of the box, but
 * requires configuration with underlying service implementations. It is also a controller 'stereotype' that can be used
 * to construct a REST API and offers an implementation of the google and freebase suggest API to offer search and
 * autocomplete functionality over ZOOMA annotations.
 * <p/>
 * For more information on the reconcilliation API, see <a href="http://code.google.com/p/google-refine/wiki/ReconciliationServiceApi">http://code.google.com/p/google-refine/wiki/ReconciliationServiceApi</a>.
 * This controller returns matching results using ZOOMA functionality behind the scenes.
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
@Controller
@RequestMapping("/annotations")
public class ZoomaAnnotationSearcher extends IdentifiableSuggestEndpoint<Annotation> {
    private AnnotationService annotationService;
    private AnnotationSearchService annotationSearchService;

    private Sorter<Annotation> annotationSorter;
    private Limiter<Annotation> annotationLimiter;

    public ZoomaAnnotationSearcher() {
        // default constructor - callers must set required properties
    }

    public ZoomaAnnotationSearcher(AnnotationService annotationService,
                                   AnnotationSearchService annotationSearchService,
                                   Sorter<Annotation> annotationSorter,
                                   Limiter<Annotation> annotationLimiter) {
        this();
        setAnnotationService(annotationService);
        setAnnotationSearchService(annotationSearchService);
        setAnnotationSorter(annotationSorter);
        setAnnotationLimiter(annotationLimiter);
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    @Autowired
    public void setAnnotationService(AnnotationService annotationService) {
        setIsValidated(false);
        this.annotationService = annotationService;
    }

    public AnnotationSearchService getAnnotationSearchService() {
        return annotationSearchService;
    }

    @Autowired
    public void setAnnotationSearchService(AnnotationSearchService annotationSearchService) {
        setIsValidated(false);
        this.annotationSearchService = annotationSearchService;
    }

    public Sorter<Annotation> getAnnotationSorter() {
        return annotationSorter;
    }

    @Autowired
    public void setAnnotationSorter(Sorter<Annotation> annotationSorter) {
        setIsValidated(false);
        this.annotationSorter = annotationSorter;
    }

    public Limiter<Annotation> getAnnotationLimiter() {
        return annotationLimiter;
    }

    @Autowired
    public void setAnnotationLimiter(Limiter<Annotation> annotationLimiter) {
        setIsValidated(false);
        this.annotationLimiter = annotationLimiter;
    }

    public Collection<Annotation> fetch() {
        return fetch(100, 0);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<Annotation> fetch(@RequestParam(value = "limit", required = false) Integer limit,
                                                      @RequestParam(value = "start", required = false) Integer start) {
        if (start == null) {
            if (limit == null) {
                return getAnnotationService().getAnnotations(100, 0);
            }
            else {
                return getAnnotationService().getAnnotations(limit, 0);
            }
        }
        else {
            if (limit == null) {
                return getAnnotationService().getAnnotations(100, start);
            }
            else {
                return getAnnotationService().getAnnotations(limit, start);
            }
        }
    }

    /**
     * Retrieves an annotation with the given URI.
     *
     * @param shortAnnotationURI the shortened form of the URI of the annotation to fetch
     * @return the annotation with the given URI
     */
    @RequestMapping(value = "/{shortAnnotationURI}", method = RequestMethod.GET)
    public @ResponseBody Annotation fetch(@PathVariable String shortAnnotationURI) {
        URI annotationURI = URIUtils.getURI(shortAnnotationURI);
        getLog().debug("Fetching " + annotationURI);
        return getAnnotationService().getAnnotation(annotationURI);
    }

    public Collection<Annotation> query(String prefix) {
        getLog().trace("Querying for " + prefix);
        validate();
        Collection<Annotation> allAnnotations = getAnnotationSearchService().searchByPrefix(prefix);
        return getAnnotationSorter().sort(allAnnotations);
    }

    public Collection<Annotation> query(String prefix, String type) {
        getLog().trace("Querying for " + prefix + ", " + type);
        validate();
        Collection<Annotation> allAnnotations = getAnnotationSearchService().searchByPrefix(type, prefix);
        return getAnnotationSorter().sort(allAnnotations);
    }

    public Collection<Annotation> query(String prefix, String type, int limit, int start) {
        getLog().trace("Querying for " + prefix + ", " + type + ", " + limit + ", " + start);
        validate();
        Collection<Annotation> allAnnotations = getAnnotationSearchService().searchByPrefix(type, prefix);
        List<Annotation> allAnnotationsList = getAnnotationSorter().sort(allAnnotations);
        return getAnnotationLimiter().limit(allAnnotationsList, limit, start);
    }

    @Override
    protected String extractElementName(Annotation annotation) {
        return annotation.getAnnotatedProperty().getPropertyValue();
    }

    @Override
    protected String extractElementTypeID() {
        return Annotation.ANNOTATION_TYPE_ID;
    }

    @Override
    protected String extractElementTypeName() {
        return Annotation.ANNOTATION_TYPE_NAME;
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
            if (limit != null) {
                if (start != null) {
                    return convertToSuggestResponse(prefix, query(prefix, type, limit, start));
                }
                else {
                    return convertToSuggestResponse(prefix, query(prefix, type, limit, 0));
                }
            }
            else {
                return convertToSuggestResponse(prefix, query(prefix, type));
            }
        }
        else {
            return convertToSuggestResponse(prefix, query(prefix));
        }
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
        // NB. Limited implementations of freebase functionality so far, we only use query, type and limiting of results
        if (type != null) {
            if (limit != null) {
                if (start != null) {
                    return convertToSearchResponse(query, query(query, type, limit, start));
                }
                else {
                    return convertToSearchResponse(query, query(query, type, limit, 0));
                }
            }
            else {
                return convertToSearchResponse(query, query(query, type));
            }
        }
        else {
            return convertToSearchResponse(query, query(query));
        }
    }

    @Override
    @RequestMapping(value = "/flyout", method = RequestMethod.GET)
    public @ResponseBody FlyoutResponse flyout(@RequestParam(value = "id") final URI shortURI) {
        return convertToFlyoutResponse(fetch(shortURI.toString()));
    }

    protected void validate() throws IllegalArgumentException {
        if (requiresValidation()) {
            Assert.notNull(getAnnotationService(), "Annotation service must not be null");
            Assert.notNull(getAnnotationSearchService(), "Annotation search service must not be null");
            Assert.notNull(getAnnotationSorter(), "Annotation sorter must not be null");
            Assert.notNull(getAnnotationLimiter(), "Annotation limiter must not be null");
            setIsValidated(true);
        }
    }
}

