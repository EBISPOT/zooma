package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;

/**
 * An implementation of a BiologicalEntity designed to be used by jackson to deserialize annotation requests.  You
 * should NOT use this implementation in code; objects are designed to be transient and in order to handle serialization
 * demands are also mutable.  If you want to code with biological entities, using {@link
 * uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity} is advisable.
 *
 * @author Tony Burdett
 * @date 15/07/13
 */
public class BiologicalEntityRequest implements BiologicalEntity {
    private static final long serialVersionUID = -1243514391866115657L;

    private String name;
    private Collection<Study> studies;
    private Collection<URI> types;

    public URI getURI() {
        return null;
    }

    public void setURI(URI uri) {
        // do nothing
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Collection<URI> getTypes() {
        return types;
    }

    public void setTypes(Collection<URI> types) {
        this.types = types;
    }

    public Collection<Study> getStudies() {
        return studies;
    }

    public void setStudies(Collection<Study> studies) {
        this.studies = studies;
    }

    @Override public String toString() {
        return "BiologicalEntityRequest {\n" +
                "  uri='" + getURI() + "'\n" +
                "  name='" + name + "'\n" +
                "  studies=" + studies + "\n}";
    }
}
