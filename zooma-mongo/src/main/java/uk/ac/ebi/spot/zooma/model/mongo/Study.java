package uk.ac.ebi.spot.zooma.model.mongo;


import lombok.Data;
import lombok.NonNull;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Data public class Study {

    @NonNull
    private String study;
    private String studyUri;

}
