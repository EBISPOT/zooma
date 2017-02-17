package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.spot.zooma.model.api.Property;

/**
 * Created by olgavrou on 08/08/2016.
 */
@Data public class UntypedProperty implements Property {

    @NonNull
    private String propertyValue;

}
