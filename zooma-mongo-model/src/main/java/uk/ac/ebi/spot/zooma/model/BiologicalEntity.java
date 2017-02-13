package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data public class BiologicalEntity {

    @NonNull
    private String bioEntity;
    @NonNull
    private Study studies;
    private String bioEntityUri;

}
