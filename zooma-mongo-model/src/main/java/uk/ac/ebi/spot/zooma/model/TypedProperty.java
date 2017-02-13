package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.spot.zooma.model.api.Property;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Data public class TypedProperty implements Property {

    @NonNull
    private String propertyType;
    @NonNull
    private String propertyValue;

    public String getPropertyType(){
        return propertyType.toLowerCase().replaceAll("_", " ");
    }
}
