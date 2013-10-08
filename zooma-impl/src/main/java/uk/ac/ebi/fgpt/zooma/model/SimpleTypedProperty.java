package uk.ac.ebi.fgpt.zooma.model;

import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.net.URI;

/**
 * A basic implementation of a TypedProperty
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public class SimpleTypedProperty extends AbstractIdentifiable implements TypedProperty {
    private String propertyType;
    private String propertyValue;

    public SimpleTypedProperty(String propertyType, String propertyValue) {
        this(null, propertyType, propertyValue);
    }

    public SimpleTypedProperty(URI uri, String propertyType, String propertyValue) {
        super(uri);
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
    }

    /**
     * A simple string that adds typing context.
     *
     * @return the property type.
     */
    public String getPropertyType() {
        return propertyType;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public boolean matches(Property property) {
        if (property == null || !(property instanceof TypedProperty)) {
            return false;
        }

        TypedProperty that = (TypedProperty) property;
        boolean typesMatch =
                propertyType != null
                        ? propertyType.equals(that.getPropertyType())
                        : that.getPropertyType() == null;
        return typesMatch &&
                (propertyValue != null
                        ? propertyValue.equals(that.getPropertyValue())
                        : that.getPropertyValue() == null);
    }

    @Override
    public int compareTo(Property o) {
        // is o typed?
        if (this.equals(o)) {
            return 0;
        }
        else {
            if (o instanceof SimpleTypedProperty) {
                SimpleTypedProperty that = (SimpleTypedProperty) o;
                String thatType = ZoomaUtils.normalizePropertyTypeString(this.getPropertyType());
                String thisType = ZoomaUtils.normalizePropertyTypeString(that.getPropertyType());

                int typeCompare = thisType.compareTo(thatType);
                if (typeCompare == 0) {
                    // if types are equal, compare values
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
                    return typeCompare;
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
        sb.append(propertyValue).append(" [").append(propertyType).append("]");
        if (getURI() != null) {
            sb.append(" <").append(getURI()).append(">");
        }
        return sb.toString();
    }
}
