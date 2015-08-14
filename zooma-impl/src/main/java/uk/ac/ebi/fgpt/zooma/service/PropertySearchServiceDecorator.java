package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Property;

import java.net.URI;
import java.util.List;

/**
 * An abstract decorator of a {@link PropertySearchService}.  You should subclass this decorator to create different
 * decorations that add functionality to property searches.
 *
 * @author Tony Burdett
 * @date 02/08/13
 */
public abstract class PropertySearchServiceDecorator implements PropertySearchService {
    private final PropertySearchService _propertySearchService;

    protected PropertySearchServiceDecorator(PropertySearchService propertySearchService) {
        this._propertySearchService = propertySearchService;
    }

    @Override public List<Property> search(String propertyValuePattern, URI... sources) {
        return _propertySearchService.search(propertyValuePattern, sources);
    }

    @Override public List<Property> search(String propertyType, String propertyValuePattern, URI... sources) {
        return _propertySearchService.search(propertyType, propertyValuePattern, sources);
    }

    @Override public List<Property> searchByPrefix(String propertyValuePrefix, URI... sources) {
        return _propertySearchService.searchByPrefix(propertyValuePrefix, sources);
    }

    @Override public List<Property> searchByPrefix(String propertyType, String propertyValuePrefix, URI... sources) {
        return _propertySearchService.searchByPrefix(propertyType, propertyValuePrefix, sources);
    }
}
