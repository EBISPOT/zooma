package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.spot.zooma.model.api.AnnotationSource;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Data public class DatabaseAnnotationSource implements AnnotationSource {

    @NonNull
    private String uri;
    @NonNull
    private String name;

    private final Type type = Type.DATABASE;
    @NonNull
    private String topic;

}
