package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.service.PropertySearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;
import uk.ac.ebi.fgpt.zooma.view.FlyoutResponse;
import uk.ac.ebi.fgpt.zooma.view.SearchResponse;
import uk.ac.ebi.fgpt.zooma.view.SuggestResponse;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Search ZOOMA for {@link Property}s matching supplied prefix values and, optionally, types.
 * <p/>
 * This class is a high level convenience implementation for searching properties.  It will work out of the box, but
 * requires configuration with underlying service implementations. It is also a controller 'stereotype' that can be used
 * to construct a REST API and offers an implementation of the google and freebase suggest API to offer search and
 * autocomplete functionality over ZOOMA properties.
 * <p/>
 * For more information on the suggest API, see <a href="http://code.google.com/p/google-refine/wiki/SuggestApi">http://code.google.com/p/google-refine/wiki/SuggestApi</a>.
 * This controller returns matching results using ZOOMA functionality behind the scenes.
 *
 * @author Tony Burdett
 * @author Drashtti Vasant
 * @date 14/03/12
 */
@Controller
@RequestMapping("/properties")
public class ZoomaPropertySearcher extends IdentifiableSuggestEndpoint<Property> {
    private PropertyService propertyService;
    private PropertySearchService propertySearchService;

    private Sorter<Property> propertySorter;
    private Limiter<Property> propertyLimiter;

    public ZoomaPropertySearcher() {
        // default constructor - callers must set required properties
    }

    public ZoomaPropertySearcher(PropertyService propertyService,
                                 PropertySearchService propertySearchService,
                                 Sorter<Property> propertySorter,
                                 Limiter<Property> propertyLimiter) {
        this();
        setPropertyService(propertyService);
        setPropertySearchService(propertySearchService);
        setPropertySorter(propertySorter);
        setPropertyLimiter(propertyLimiter);
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    @Autowired
    public void setPropertyService(PropertyService propertyService) {
        setIsValidated(false);
        this.propertyService = propertyService;
    }

    public PropertySearchService getPropertySearchService() {
        return propertySearchService;
    }

    @Autowired
    public void setPropertySearchService(PropertySearchService propertySearchService) {
        setIsValidated(false);
        this.propertySearchService = propertySearchService;
    }

    public Sorter<Property> getPropertySorter() {
        return propertySorter;
    }

    @Autowired
    public void setPropertySorter(Sorter<Property> propertySorter) {
        setIsValidated(false);
        this.propertySorter = propertySorter;
    }

    public Limiter<Property> getPropertyLimiter() {
        return propertyLimiter;
    }

    @Autowired
    public void setPropertyLimiter(Limiter<Property> propertyLimiter) {
        setIsValidated(false);
        this.propertyLimiter = propertyLimiter;
    }

    public Collection<Property> fetch() {
        return fetch(null, 100, 0);
    }

    public Collection<Property> fetch(String type) {
        return fetch(type, 100, 0);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<Property> fetch(@RequestParam(value = "type", required = false) String type,
                                                    @RequestParam(value = "limit", required = false) Integer limit,
                                                    @RequestParam(value = "start", required = false) Integer start) {
        if (type != null && type.isEmpty()) {
            return getPropertyService().getMatchedTypedProperties(type);
        }
        else {
            if (start == null) {
                if (limit == null) {
                    return getPropertyService().getProperties(100, 0);
                }
                else {
                    return getPropertyService().getProperties(limit, 0);
                }
            }
            else {
                if (limit == null) {
                    return getPropertyService().getProperties(100, start);
                }
                else {
                    return getPropertyService().getProperties(limit, start);
                }
            }
        }
    }

    /**
     * Retrieves a property with the given URI.
     *
     * @param shortPropertyURI the short form of the URI of the property to fetch
     * @return the property with the given URI
     */
    @RequestMapping(value = "/{shortPropertyURI}", method = RequestMethod.GET)
    public @ResponseBody Property fetchByURI(@PathVariable String shortPropertyURI) {
        URI propertyURI = URIUtils.getURI(getPropertiesMapAdapter().getPropertyMap(), shortPropertyURI);
        getLog().debug("Fetching " + propertyURI);
        return getPropertyService().getProperty(propertyURI);
    }

    public Collection<Property> query(String prefix) {
        getLog().trace("Querying for '" + prefix + "'");
        validate();
        return getPropertySearchService().searchByPrefix(prefix);
    }

    public Collection<Property> query(String prefix, String type) {
        getLog().trace("Querying for '" + prefix + "', '" + type + "'");
        validate();
        return getPropertySearchService().searchByPrefix(prefix, type);
    }

    public Collection<Property> query(String prefix, String type, int limit, int start) {
        getLog().trace("Querying for '" + prefix + "', '" + type + "', " + limit + ", " + start);
        validate();
        Map<Property, Float> allProperties = getPropertySearchService().searchAndScoreByPrefix(prefix, type);
        List<Property> allPropertiesList = getPropertySorter().sort(allProperties);
        return getPropertyLimiter().limit(allPropertiesList, limit, start);
    }

    @Override
    protected String extractElementName(Property property) {
        return property.getPropertyValue();
    }

    @Override
    protected String extractElementTypeID() {
        return Property.PROPERTY_TYPE_ID;
    }

    @Override
    protected String extractElementTypeName() {
        return Property.PROPERTY_TYPE_NAME;
    }

    @Override
    @RequestMapping(value = "suggest", method = RequestMethod.GET)
    public @ResponseBody SuggestResponse suggest(
            @RequestParam(value = "prefix") String prefix,
            @RequestParam(value = "type", required = false) String type,
            @SuppressWarnings("UnusedParameters")
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
            @RequestParam(value = "query", required = false) String query,
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
        if (query == null) {
            query = "";
        }

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
        return convertToFlyoutResponse(fetchByURI(shortURI.toString()));
    }

    /**
     * Validates that this class has been correctly set up, with all required properties set to non-null values.
     *
     * @throws IllegalArgumentException if a required property is not set
     */
    protected void validate() throws IllegalArgumentException {
        if (requiresValidation()) {
            Assert.notNull(getPropertyService(), "Property service must not be null");
            Assert.notNull(getPropertySearchService(), "Property search service must not be null");
            Assert.notNull(getPropertySorter(), "Property sorter must not be null");
            Assert.notNull(getPropertyLimiter(), "Property limiter must not be null");
            setIsValidated(true);
        }
    }
}
