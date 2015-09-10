package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.service.PropertySearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;

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
public class ZoomaProperties {
    private PropertyService propertyService;
    private PropertySearchService propertySearchService;

    private Sorter<Property> propertySorter;
    private Limiter<Property> propertyLimiter;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public ZoomaProperties(PropertyService propertyService,
                           PropertySearchService propertySearchService,
                           Sorter<Property> propertySorter,
                           Limiter<Property> propertyLimiter) {
        this.propertyService = propertyService;
        this.propertySearchService = propertySearchService;
        this.propertySorter = propertySorter;
        this.propertyLimiter = propertyLimiter;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    public PropertySearchService getPropertySearchService() {
        return propertySearchService;
    }

    public Sorter<Property> getPropertySorter() {
        return propertySorter;
    }

    public Limiter<Property> getPropertyLimiter() {
        return propertyLimiter;
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
        URI propertyURI = URIUtils.getURI(shortPropertyURI);
        getLog().debug("Fetching " + propertyURI);
        return getPropertyService().getProperty(propertyURI);
    }

    public List<Property> query(String prefix) {
        getLog().trace("Querying for '" + prefix + "'");
        return getPropertySearchService().searchByPrefix(prefix);
    }

    public List<Property> query(String prefix, URI[] requiredSources) {
        getLog().trace("Querying for '" + prefix + "'");
        return getPropertySearchService().searchByPrefix(prefix, requiredSources);
    }

    public List<Property> query(String prefix, String type) {
        getLog().trace("Querying for '" + prefix + "', '" + type + "'");
        return getPropertySearchService().searchByPrefix(type, prefix);
    }

    public List<Property> query(String prefix, String type, URI[] requiredSources) {
        getLog().trace("Querying for '" + prefix + "', '" + type + "'");
        return getPropertySearchService().searchByPrefix(type, prefix, requiredSources);
    }

    public List<Property> query(String prefix, String type, int limit, int start) {
        getLog().trace("Querying for '" + prefix + "', '" + type + "', " + limit + ", " + start);
        Collection<Property> allProperties = getPropertySearchService().searchByPrefix(type, prefix);
        List<Property> allPropertiesList = getPropertySorter().sort(allProperties);
        return getPropertyLimiter().limit(allPropertiesList, limit, start);
    }

    public List<String> suggest(String prefix) {
        getLog().trace("Querying for '" + prefix + "'");
        return getPropertySearchService().suggest(prefix);
    }

    public List<String> suggest(String prefix, URI[] requiredSources) {
        getLog().trace("Querying for '" + prefix + "'");
        return getPropertySearchService().suggest(prefix, requiredSources);
    }
}
