package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NonNull;

import java.util.Collection;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data public class BiologicalEntity {

    @NonNull
    private String bioEntity;
    @NonNull
    private Collection<Study> studies;
    private String bioEntityUri;

}
