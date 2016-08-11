package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by olgavrou on 09/08/2016.
 */
@Document(collection = "properties")
public class SimpleProperty extends SimpleDocument implements Property{

    private String propertyValue;

    public SimpleProperty(String id, String propertyValue) {
        super(id);
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

    @Override
    public int compareTo(Property o) {
        return 0;
    }
}
