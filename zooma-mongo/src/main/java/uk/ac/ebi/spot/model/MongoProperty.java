package uk.ac.ebi.spot.model;

/**
 * Created by olgavrou on 09/08/2016.
 */
public class MongoProperty implements Property{

    private String propertyValue;

    public MongoProperty(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public String getPropertyValue() {
        return this.propertyValue;
    }

    @Override
    public boolean matches(Property property) {
        return false;
    }


}
