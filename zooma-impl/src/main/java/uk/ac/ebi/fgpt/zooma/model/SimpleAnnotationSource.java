package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * A basic implementation of an Annotation Source object, declaring the source of an annotation
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 04/10/13
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

}
