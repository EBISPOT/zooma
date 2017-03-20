package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Data
public class Property {

    @NonNull
    @Indexed
    private String propertyType;
    @NonNull
    @Indexed
    private String propertyValue;

    public String getPropertyType(){
        return propertyType.toLowerCase().replaceAll("_", " ");
    }
}
