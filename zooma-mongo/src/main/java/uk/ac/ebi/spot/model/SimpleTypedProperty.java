package uk.ac.ebi.spot.model;

/**
 * Created by olgavrou on 03/08/2016.
 */
public class SimpleTypedProperty extends SimpleProperty implements TypedProperty {

    private String propertyType;

    public SimpleTypedProperty(String id, String propertyType, String propertyValue) {
        super(id, propertyValue);
        this.propertyType = propertyType;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

}
