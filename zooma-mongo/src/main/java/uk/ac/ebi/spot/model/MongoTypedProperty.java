package uk.ac.ebi.spot.model;

/**
 * Created by olgavrou on 03/08/2016.
 */
public class MongoTypedProperty extends MongoProperty implements TypedProperty {

    private String propertyType;

    public MongoTypedProperty(String propertyType, String propertyValue) {
        super(propertyValue);
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
