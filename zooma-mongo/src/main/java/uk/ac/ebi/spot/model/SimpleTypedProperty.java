package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 03/08/2016.
 */
public class SimpleTypedProperty implements TypedProperty {

    private String propertyType;
    private String propertyValue;

    public SimpleTypedProperty(String propertyType, String propertyValue) {
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean matches(Property property) {
        return false;
    }

    @Override
    public int compareTo(Property o) {
        return 0;
    }

    @Override
    public URI getURI() {
        return null;
    }
}
