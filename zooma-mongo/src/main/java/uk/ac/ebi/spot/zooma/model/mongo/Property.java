package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Data public class Property {

    @NonNull
    private String propertyType;
    @NonNull
    private String propertyValue;

    public String getPropertyType(){
        return propertyType.toLowerCase().replaceAll("_", " ");
    }
}
