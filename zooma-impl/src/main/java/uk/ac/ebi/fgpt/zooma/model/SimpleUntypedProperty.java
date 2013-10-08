package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * A basic implementation of a Property, with no typing information
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public class SimpleUntypedProperty extends AbstractIdentifiable implements Property {
    private String propertyValue;

    public SimpleUntypedProperty(String propertyValue) {
        this(null, propertyValue);
    }

    public SimpleUntypedProperty(URI uri, String propertyValue) {
        super(uri);
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public boolean matches(Property property) {
        return property != null && (
                propertyValue != null
                        ? propertyValue.equals(property.getPropertyValue())
                        : property.getPropertyValue() == null);
    }


    @Override
    public int compareTo(Property o) {
        // is o typed?
        if (this.equals(o)) {
            return 0;
        }
        else {
            if (o instanceof SimpleUntypedProperty) {
                SimpleUntypedProperty that = (SimpleUntypedProperty) o;
                int valCompare = this.getPropertyValue().compareTo(that.getPropertyValue());
                if (valCompare == 0) {
                    // values also equal, so return URI comparison
                    // N.B. nothing else to compare whilst remaining consistent with equals()
                    return this.getURI().compareTo(that.getURI());
                }
                else {
                    return valCompare;
                }
            }
            else {
                // not equals, not same class, so just return value comparision
                return this.getPropertyValue().compareTo(o.getPropertyValue());
            }
        }
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property '").append(propertyValue).append("'");
        if (getURI() != null) {
            sb.append(" <").append(getURI()).append(">");
        }
        return sb.toString();
    }
}
