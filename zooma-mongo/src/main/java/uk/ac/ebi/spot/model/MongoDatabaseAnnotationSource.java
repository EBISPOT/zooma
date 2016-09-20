package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 05/08/2016.
 */
public class MongoDatabaseAnnotationSource extends MongoAnnotationSource {
    public MongoDatabaseAnnotationSource(URI uri, String name) {
        super(uri, name, Type.DATABASE);
    }
}
