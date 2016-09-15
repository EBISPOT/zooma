package uk.ac.ebi.spot.repositories.custom;

import uk.ac.ebi.spot.model.Property;

import java.util.List;


/**
 * This custom interface of the {@link uk.ac.ebi.spot.repositories.PropertyRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.mongodb.repository.MongoRepository}, to be constructed. An implementation of this interface
 * can be defined as long as it is named "PropertyRepositoryImpl" and as long as the {@link uk.ac.ebi.spot.repositories.PropertyRepository}
 * extends this interface.
 *
 * Created by olgavrou on 15/09/2016.
 */
public interface CustomPropertyRepository {

    /**
     * Retrieves all distinct properties from a zooma datasource.
     *
     * @return a list of all distinct properties
     */
    List<Property> findDistinctAnnotatedProperties();

    /**
     * Retrieves all property types from a zooma datasource.  No ordering is assumed, and repeat calls to this method
     * are not obliged to return results in the same order.
     *
     * @return the list of all property types
     */
    List<String> findAllPropertyTypes();

    /**
     * Retrieves a list of properties from a zooma datasource by exactly matching on type.
     *
     * @param type the type to match on
     * @return a list of matching properties.
     */
    List<Property> findPropertyFromPropertyType(String type);

    /**
     * Retrieves a property from a zooma datasource by exactly matching on value.  Returned properties must have NO
     * type
     *
     * @param value the value to match on
     * @return the matching property
     */
    List<Property> findPropertyFromPropertyValue(String value);

    /**
     * Retrieves a property from a zooma datasource by exactly matching on type and value
     *
     * @param type  the type to match on (can be null if value is supplied)
     * @param value the value to match on (can be null if type supplied)
     * @return the matching property
     */
    List<Property> findPropertyFromPropertyTypeAndPropertyValue(String type, String value);
}
