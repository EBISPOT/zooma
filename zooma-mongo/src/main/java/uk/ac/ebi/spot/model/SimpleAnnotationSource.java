package uk.ac.ebi.spot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Document(collection = "annotationSources")
public class SimpleAnnotationSource extends SimpleDocument implements AnnotationSource{

    private URI source;
    private Type type;
    private String name;


    public SimpleAnnotationSource(URI source, String name, Type type) {
        this.source = source;
        this.name = name;
        this.type = type;
    }


    @Override public URI getURI() {
        return source;
    }

    @Override public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SimpleAnnotationSource{" +
                "source=" + source +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
