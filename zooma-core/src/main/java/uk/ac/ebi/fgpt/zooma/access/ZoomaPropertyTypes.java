package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeSearchService;
import uk.ac.ebi.fgpt.zooma.service.PropertyTypeService;
import uk.ac.ebi.fgpt.zooma.util.Limiter;
import uk.ac.ebi.fgpt.zooma.util.Sorter;

import java.net.URI;
import java.util.Collection;
import java.util.List;

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
public class ZoomaPropertyTypes extends SourceFilteredEndpoint<String, String> {
    private PropertyTypeService propertyTypeService;
    private PropertyTypeSearchService propertyTypeSearchService;

    private Sorter<String> propertyTypeSorter;
    private Limiter<String> propertyTypeLimiter;

    @Autowired
    public ZoomaPropertyTypes(PropertyTypeService propertyTypeService,
                              PropertyTypeSearchService propertyTypeSearchService,
                              Sorter<String> propertyTypeSorter,
                              Limiter<String> propertyTypeLimiter) {
        this.propertyTypeService = propertyTypeService;
        this.propertyTypeSearchService = propertyTypeSearchService;
        this.propertyTypeSorter = propertyTypeSorter;
        this.propertyTypeLimiter = propertyTypeLimiter;
    }

    public PropertyTypeService getPropertyTypeService() {
        return propertyTypeService;
    }

    public PropertyTypeSearchService getPropertyTypeSearchService() {
        return propertyTypeSearchService;
    }

    public Sorter<String> getPropertyTypeSorter() {
        return propertyTypeSorter;
    }

    public Limiter<String> getPropertyTypeLimiter() {
        return propertyTypeLimiter;
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
        return getPropertyTypeSearchService().searchByPrefix(prefix);
    }

    public Collection<String> query(String prefix, URI[] requiredSources) {
        return getPropertyTypeSearchService().searchByPrefix(prefix, requiredSources);
    }

    public Collection<String> query(String prefix, int limit, int start) {
        Collection<String> allTypes = getPropertyTypeSearchService().searchByPrefix(prefix);
        List<String> allTypesList = getPropertyTypeSorter().sort(allTypes);
        return getPropertyTypeLimiter().limit(allTypesList, limit, start);
    }
}
