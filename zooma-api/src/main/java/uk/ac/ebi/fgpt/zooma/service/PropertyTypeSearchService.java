package uk.ac.ebi.fgpt.zooma.service;

import java.util.Collection;
import java.util.Map;

/**
 * A ZOOMA service that allows searching over the {@link uk.ac.ebi.fgpt.zooma.model.TypedProperty}s known to ZOOMA. This
 * service supports search queries for property types by a string searching algorithm defined by each implementation
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public interface PropertyTypeSearchService {
    /**
     * Search the set of properties known to ZOOMA to identify those property types that match the supplied pattern
     *
     * @param propertyTypePattern the property type that should be searched for
     * @return a collection of matching property type strings
     */
    Collection<String> search(String propertyTypePattern);

    /**
     * Search the set of properties known to ZOOMA to identify those property types that match the supplied pattern
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     *
     * @param propertyTypePrefix the property value that should be searched for
     * @return a collection of matching property type strings
     */
    Collection<String> searchByPrefix(String propertyTypePrefix);
}
