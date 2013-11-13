package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Property;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * A data access object that defines methods to create, retrieve, update and delete properties from a ZOOMA datasource.
 *
 * @author Tony Burdett
 * @date 29/03/12
 */
public interface PropertyDAO extends ZoomaDAO<Property> {
    /**
     * Retrieves all property types from a zooma datasource.  No ordering is assumed, and repeat calls to this method
     * are not obliged to return results in the same order.
     *
     * @return the collection of all property types
     */
    Collection<String> readTypes();

    /**
     * Retrieves a collection of property types from a zooma datasource, limited to the given size starting at the given
     * index. Ordering should be consistent across repeat calls of this method.
     *
     * @return a list of property types of the given size, starting from the supplied index
     */
    List<String> readTypes(int size, int start);

    /**
     * Retrieves a property type from a zooma datasource given the properties URI.
     *
     * @param uri the identifier of a property
     * @return the property type for the property with this URI
     */
    String readType(URI uri);

    /**
     * Retrieves a property from a zooma datasource by exactly matching on type and value
     *
     * @param type  the type to match on
     * @param value the value to match on
     * @return the matching property
     */
    Property readByTypeAndValue(String type, String value);

    /**
     * Retrieves a property from a zooma datasource by exactly matching on value.  Returned properties must have NO
     * type
     *
     * @param value the value to match on
     * @return the matching property
     */
    Property readByValue(String value);

    /**
     * Retrieves a collection of properties from a zooma datasource by exactly matching on type.
     *
     * @param type the type to match on
     * @return a collection of matching properties.
     */
    Collection<Property> readByType(String type);
}
