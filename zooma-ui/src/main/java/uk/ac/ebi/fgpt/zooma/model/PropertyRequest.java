package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * An implementation of a TypedProperty designed to be used by jackson to deserialize property requests.  You should NOT
 * use this implementation in code; objects are designed to be transient and in order to handle serialization demands
 * are also mutable.  If you want to code with properties, using {@link uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty}
 * is advisable.
 *
 * @author Tony Burdett
 * @date 15/07/13
 */
public class PropertyRequest implements TypedProperty {
    private static final long serialVersionUID = 3928428716181660097L;

    private String propertyType;
    private String propertyValue;
    private URI uri;

    public void setURI(URI uri) {
        this.uri = uri;
    }

    @Override public URI getURI() {
        return uri;
    }


    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override public boolean matches(Property property) {
        throw new UnsupportedOperationException("Unable to test equality on transient PropertyRequest - " +
                                                        "save this property first!");
    }

    @Override public int compareTo(Property o) {
        throw new UnsupportedOperationException("Unable to compare transient PropertyRequests - " +
                                                        "save this property first!");
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(propertyValue).append(" [").append(propertyType).append("]");
        if (getURI() != null) {
            sb.append(" <").append(getURI()).append(">");
        }
        return sb.toString();
    }
}
