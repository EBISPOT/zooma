package uk.ac.ebi.spot.model;

import org.springframework.stereotype.Component;

/**
 * Created by olgavrou on 25/10/2016.
 */
public class AnnotationProperty {
    private String propertyValue;

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
