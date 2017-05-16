package uk.ac.ebi.spot.zooma.model.mongo;

import java.util.Collection;

/**
 * Created by olgavrou on 04/08/2016.
 */
public interface AnnotationSource {

    String getUri();
    String getName();
    Type getType();
    Collection<String> getTopic();


    enum Type {
        DATABASE,
        ONTOLOGY;

        public static Type lookup(String id) {
            for (Type e : Type.values()) {
                if (e.name().equals(id)) {
                    return e;
                }
            }
            return Type.DATABASE;
        }
    }
}
