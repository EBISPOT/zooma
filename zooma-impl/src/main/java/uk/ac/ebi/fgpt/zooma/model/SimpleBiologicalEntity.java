package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * A basic implementation of a Biological Entity
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class SimpleBiologicalEntity extends AbstractIdentifiable implements BiologicalEntity {
    private static final long serialVersionUID = -5982916971163589494L;

    private String name;
    private Collection<Study> studies;

    public SimpleBiologicalEntity(URI uri, String name, Study... studies) {
        super(uri);
        this.name = name;
        this.studies = new HashSet<>();
        Collections.addAll(this.studies, studies);
    }

    @Override public String getName() {
        return name;
    }

    @Override public Collection<Study> getStudies() {
        return studies;
    }

    @Override public String toString() {
        return "SimpleBiologicalEntity {\n" +
                "  uri='" + getURI() + "'\n" +
                "  name='" + name + "'\n" +
                "  studies=" + studies + "\n}";
    }
}
