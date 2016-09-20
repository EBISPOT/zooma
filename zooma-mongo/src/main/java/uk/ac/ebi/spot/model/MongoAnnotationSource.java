package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 04/08/2016.
 */
public class MongoAnnotationSource implements AnnotationSource {

    private URI uri;
    private Type type;
    private String name;


    public MongoAnnotationSource(URI uri, String name, Type type) {
        this.uri = uri;
        this.name = name;
        this.type = type;
    }


    @Override public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public URI getUri() { return uri; }

    @Override
    public String toString() {
        return "MongoAnnotationSource{" +
                "uri=" + uri +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
