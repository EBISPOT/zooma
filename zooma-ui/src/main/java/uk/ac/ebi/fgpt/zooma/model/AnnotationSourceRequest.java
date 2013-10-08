package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * An implementation of AnnotationSource designed to be used by jackson to deserialize annotation source requests.  You
 * should NOT use this implementation in code; objects are designed to be transient and in order to handle serialization
 * demands are also mutable.  If you want to code with annotation source objects, using {@link
 * uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSource} is advisable.
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public class AnnotationSourceRequest implements AnnotationSource {
    private static final long serialVersionUID = 3981174697979093334L;

    private URI source;
    private Type type;

    public URI getURI() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override public String toString() {
        return "AnnotationSourceRequest{" +
                "source=" + source +
                ", type=" + type +
                '}';
    }
}
