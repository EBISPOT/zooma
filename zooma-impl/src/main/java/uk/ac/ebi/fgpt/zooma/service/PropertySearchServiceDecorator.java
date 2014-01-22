package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Property;

import java.util.Collection;
import java.util.Map;

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

    @Override public Collection<Property> search(String propertyValuePattern) {
        return _propertySearchService.search(propertyValuePattern);
    }

    @Override public Collection<Property> search(String propertyType, String propertyValuePattern) {
        return _propertySearchService.search(propertyType, propertyValuePattern);
    }

    @Override public Collection<Property> searchByPrefix(String propertyValuePrefix) {
        return _propertySearchService.searchByPrefix(propertyValuePrefix);
    }

    @Override public Collection<Property> searchByPrefix(String propertyType, String propertyValuePrefix) {
        return _propertySearchService.searchByPrefix(propertyType, propertyValuePrefix);
    }
}
