package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 04/08/2016.
 */
public class SimpleAnnotationSource implements AnnotationSource {

    private URI source;
    private Type type;
    private String name;


    public SimpleAnnotationSource(URI source, String name, Type type) {
        this.source = source;
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

    public URI getSource() { return source; }

    @Override
    public String toString() {
        return "SimpleAnnotationSource{" +
                "source=" + source +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
