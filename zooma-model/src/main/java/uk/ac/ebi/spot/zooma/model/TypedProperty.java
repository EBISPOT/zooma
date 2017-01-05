package uk.ac.ebi.spot.zooma.model;

/**
 * Any {@link Property} that also includes a string describing the property type.
 *
 * @author Tony Burdett
 * @date 28/03/12
 */
public interface TypedProperty extends Property {
    /**
     * Returns the property type for this property.
     *
     * @return the property type
     */
    String getPropertyType();
}
