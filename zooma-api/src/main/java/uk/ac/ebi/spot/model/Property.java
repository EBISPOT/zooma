package uk.ac.ebi.spot.model;

/**
 * A property represents a basic text expression that adorns any {@link BiologicalEntity}.
 * <p/>
 * At it's simplest, a property is just a wrapper around a {@link String} with an additional {@link #matches(Property)}
 * method.  Implementations are free to add typing information or other context.
 * <p/>
 * One additional extension of this interface are provided - {@link TypedProperty}. During searches,
 * <code>Properties</code> should match to all <code>Properties</code> that share the same property value.  However, the
 * inverse is not true - <code>TypedProperties</code> should only match other <code>TypedProperties</code> with matching
 * property type and value.
 * <p/>
 * New implementations of this interface are free to define as much context information as they wish: however, the basic
 * one-way mapping contract should always be followed, such that properties with less information should assume that the
 * additional context is missing and unknown, and should therefore match those with additional context.
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public interface Property extends Comparable<Property> {
    /**
     * Returns the property value for this property.
     *
     * @return the property value
     */
    String getPropertyValue();

    /**
     * Returns true if the supplied property matches this one.  Note that unlike standard Java implementation of {@link
     * #equals(Object)}, this method is not symmetric - by contract, properties with less context should match those
     * with extra context information, but properties that supply context information should match ONLY those that share
     * all the same context information.  For this reason <code>a.matches(b)</code> can return true when
     * <code>b.matches(a)</code> returns false.
     *
     * @param property the property to compare to
     * @return true if these properties match, false otherwise
     */
    boolean matches(Property property);
}

