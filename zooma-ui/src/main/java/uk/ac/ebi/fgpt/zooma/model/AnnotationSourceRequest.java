package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * An implementation of AnnotationSource designed to be used by jackson to deserialize annotation uri requests.  You
 * should NOT use this implementation in code; objects are designed to be transient and in order to handle serialization
 * demands are also mutable.  If you want to code with annotation uri objects, using {@link
 * uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSource} is advisable.
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public class AnnotationSourceRequest implements AnnotationSource {
    private static final long serialVersionUID = 3981174697979093334L;

    private URI uri;
    private Type type;
    private String name;

    public String getName() {
        return name;
    }

    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override public String toString() {
        return "AnnotationSourceRequest{" +
                "uri=" + uri +
                ", type=" + type +
                ", name=" + name +
                '}';
    }
}
