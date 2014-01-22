package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeSearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Search ZOOMA for property types matching supplied prefix values.
 * <p/>
 * This class is a high level convenience implementation for searching property types.  It will work out of the box, but
 * requires configuration with underlying service implementations. It is also a controller 'stereotype' that can be used
 * to construct a REST API and offers an implementation of the google and freebase suggest API to offer search and
 * autocomplete functionality over ZOOMA property types.
 * <p/>
 * For more information on the suggest API, see <a href="http://code.google.com/p/google-refine/wiki/SuggestApi">http://code.google.com/p/google-refine/wiki/SuggestApi</a>.
 * This controller returns matching results using ZOOMA functionality behind the scenes.
 *
 * @author Tony Burdett
 * @date 23/03/12
 */
@Controller
@RequestMapping("/properties/types")
public class ZoomaPropertyTypeSearcher extends SuggestEndpoint<String, String> {
    private PropertyTypeService propertyTypeService;
    private PropertyTypeSearchService propertyTypeSearchService;

    private Sorter<String> propertyTypeSorter;
    private Limiter<String> propertyTypeLimiter;

    private boolean isValidated = false;

    public ZoomaPropertyTypeSearcher() {
        // default constructor - callers must set required properties
    }

    public ZoomaPropertyTypeSearcher(PropertyTypeService propertyTypeService,
                                     PropertyTypeSearchService propertyTypeSearchService,
                                     Sorter<String> propertyTypeSorter,
                                     Limiter<String> propertyTypeLimiter) {
        this();
        setPropertyTypeService(propertyTypeService);
        setPropertyTypeSearchService(propertyTypeSearchService);
        setPropertyTypeSorter(propertyTypeSorter);
        setPropertyTypeLimiter(propertyTypeLimiter);
    }

    public PropertyTypeService getPropertyTypeService() {
        return propertyTypeService;
    }

    @Autowired
    public void setPropertyTypeService(PropertyTypeService propertyTypeService) {
        setIsValidated(false);
        this.propertyTypeService = propertyTypeService;
    }

    public PropertyTypeSearchService getPropertyTypeSearchService() {
        return propertyTypeSearchService;
    }

    @Autowired
    public void setPropertyTypeSearchService(PropertyTypeSearchService propertyTypeSearchService) {
        setIsValidated(false);
        this.propertyTypeSearchService = propertyTypeSearchService;
    }

    public Sorter<String> getPropertyTypeSorter() {
        return propertyTypeSorter;
    }

    @Autowired
    public void setPropertyTypeSorter(Sorter<String> propertyTypeSorter) {
        setIsValidated(false);
        this.propertyTypeSorter = propertyTypeSorter;
    }

    public Limiter<String> getPropertyTypeLimiter() {
        return propertyTypeLimiter;
    }

    @Autowired
    public void setPropertyTypeLimiter(Limiter<String> propertyTypeLimiter) {
        setIsValidated(false);
        this.propertyTypeLimiter = propertyTypeLimiter;
    }

    public Collection<String> fetch() {
        return fetch(100, 0);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<String> fetch(@RequestParam(value = "limit", required = false) Integer limit,
                                                  @RequestParam(value = "start", required = false) Integer start) {
        if (start == null) {
            if (limit == null) {
                return getPropertyTypeService().getPropertyTypes(100, 0);
            }
            else {
                return getPropertyTypeService().getPropertyTypes(limit, 0);
            }
        }
        else {
            if (limit == null) {
                return getPropertyTypeService().getPropertyTypes(100, start);
            }
            else {
                return getPropertyTypeService().getPropertyTypes(limit, start);
            }
        }
    }

    public Collection<String> query(String prefix) {
        validate();
        return getPropertyTypeSearchService().searchByPrefix(prefix);
    }

    public Collection<String> query(String prefix, int limit, int start) {
        validate();
        Collection<String> allTypes = getPropertyTypeSearchService().searchByPrefix(prefix);
        List<String> allTypesList = getPropertyTypeSorter().sort(allTypes);
        return getPropertyTypeLimiter().limit(allTypesList, limit, start);
    }

    @Override protected String extractElementID(String s) {
        try {
            String encoded = URLEncoder.encode(s, "UTF-8");
            return "http://www.ebi.ac.uk".concat(Property.PROPERTYTYPE_TYPE_ID).concat("/").concat(encoded);
        }
        catch (UnsupportedEncodingException e) {
            getLog().error("Caught an UnsupportedEncodingException", e);
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    @Override protected String extractElementName(String s) {
        return s;
    }

    @Override protected String extractElementTypeID() {
        return Property.PROPERTYTYPE_TYPE_ID;
    }

    @Override protected String extractElementTypeName() {
        return Property.PROPERTYTYPE_TYPE_NAME;
    }

    @Override
    @RequestMapping(value = "suggest", method = RequestMethod.GET)
    public @ResponseBody SuggestResponse suggest(
            @RequestParam(value = "prefix") String prefix,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "type_strict", required = false) String type_strict,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start) {
        // check each non-required argument and defer to appropriate query form
        // NB. we don't use type or type_strict param anywhere
        if (limit != null) {
            if (start != null) {
                return convertToSuggestResponse(prefix, query(prefix, limit, start));
            }
            else {
                return convertToSuggestResponse(prefix, query(prefix, limit, 0));
            }
        }
        else {
            return convertToSuggestResponse(prefix, query(prefix));
        }
    }

    @Override
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody SearchResponse search(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "exact", required = false, defaultValue = "false") Boolean exact,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "start", required = false, defaultValue = "0") Integer start,
            @RequestParam(value = "prefixed", required = false, defaultValue = "false") Boolean prefixed,
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "html_escape", required = false, defaultValue = "true") Boolean html_escape,
            @RequestParam(value = "indent", required = false, defaultValue = "false") Boolean indent,
            @RequestParam(value = "mql_output", required = false) String mql_output) {
        // NB. Limited implementations of freebase functionality so far, we only use query, type and limiting of results
        if (limit != null) {
            if (start != null) {
                return convertToSearchResponse(query, query(query, limit, start));
            }
            else {
                return convertToSearchResponse(query, query(query, limit, 0));
            }
        }
        else {
            return convertToSearchResponse(query, query(query));
        }
    }

    @Override
    @RequestMapping(value = "/flyout", method = RequestMethod.GET)
    public @ResponseBody FlyoutResponse flyout(@RequestParam(value = "type_name") final String type_name) {
        return convertToFlyoutResponse(type_name);
    }

    protected void setIsValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    protected boolean requiresValidation() {
        return !isValidated;
    }

    /**
     * Validates that this class has been correctly set up, with all required properties set to non-null values.
     *
     * @throws IllegalArgumentException if a required property is not set
     */
    protected void validate() throws IllegalArgumentException {
        if (requiresValidation()) {
            Assert.notNull(getPropertyTypeService(), "Property type service must not be null");
            Assert.notNull(getPropertyTypeSearchService(), "Property type search service must not be null");
            Assert.notNull(getPropertyTypeSorter(), "Property type sorter must not be null");
            Assert.notNull(getPropertyTypeLimiter(), "Property type limiter must not be null");
            setIsValidated(true);
        }
    }
}
