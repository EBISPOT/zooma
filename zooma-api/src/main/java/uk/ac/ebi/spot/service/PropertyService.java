package uk.ac.ebi.spot.service;


import uk.ac.ebi.spot.exception.ZoomaUpdateException;
import uk.ac.ebi.spot.model.Property;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows direct retrieval of a {@link uk.ac.ebi.spot.model.Property} or sets of Properties
 * known to ZOOMA.
 * <p/>
 * This is a high level interface that provides some degree of abstraction over the underlying datasource
 * implementation.  Most implementations will delegate requests to an {@link uk.ac.ebi.spot.datasource
 * .PropertyDAO} as and when required.  If any caching or indexing strategies are required, they should be implemented
 * here rather than at the DAO level.
 *
 * @author Tony Burdett
 * @date 23/03/12
 */
public interface PropertyService {
    /**
     * Returns the collection of all properties in ZOOMA
     *
     * @return all properties
     */
    Collection<Property> getProperties();

    /**
     * Returns a subset of all properties in ZOOMA, limited by the size of the set and the start index.
     *
     * @param limit the size of the collection that should be returned
     * @param start the starting index for the set to be returned
     * @return a collection of properties of size 'limit'
     */
    Collection<Property> getProperties(int limit, int start);

    /**
     * Returns a property from ZOOMA, given it's qualified shortened URI.
     *
     * @param shortname the identifier of this property
     * @return the property with this URI
     */
    Property getProperty(String shortname);

    /**
     * Returns a property from ZOOMA, given it's URI.
     *
     * @param uri the identifier of this property
     * @return the property with this URI
     */
    Property getProperty(URI uri);

    /**
     * Returns a property from ZOOMA, assuming an exact match on property type and/or value
     *
     * @param type  the type of the property to fetch (can be empty or null)
     * @param value the value of the property to fetch (can be null if a type is supplied)
     * @return a single matching property, if one is present, or null otherwise
     */
    Collection<Property> getMatchedTypedProperty(String type, String value);

    /**
     * Returns a property from ZOOMA, assuming an exact match on property value.  Typed properties will not be returned
     * by this method
     *
     * @param value the value of the property to fetch
     * @return a single matching property, if one is present, or null otherwise
     */
    Property getMatchedUntypedProperty(String value);

    /**
     * Returns a collection of all properties in ZOOMA that have an exact match on the supplied type
     *
     * @param type the type of the property to fetch (can be empty or null)
     * @return a collection of matching properties, if present, or an empty
     */
    Collection<Property> getMatchedTypedProperties(String type);

    /**
     * Saves the given property in ZOOMA.  Returns the updated property reference (which should now include an assigned
     * URI, if there wasn't one already)
     *
     * @param property the property to save (will be created if it doesn't already exist)
     * @return an updated reference to the property, if necessary
     */
    Property saveProperty(Property property) throws ZoomaUpdateException;

}
