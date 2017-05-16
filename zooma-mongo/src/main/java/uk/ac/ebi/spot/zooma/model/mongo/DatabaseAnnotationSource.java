package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NonNull;

import java.util.Collection;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Data
public class DatabaseAnnotationSource implements AnnotationSource {

    @NonNull
    private String uri;
    @NonNull
    private String name;

    private final Type type = Type.DATABASE;
    @NonNull
    private Collection<String> topic;

}
