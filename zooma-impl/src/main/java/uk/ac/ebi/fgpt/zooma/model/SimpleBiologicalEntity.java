package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation of a Biological Entity
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class SimpleBiologicalEntity extends AbstractIdentifiable implements BiologicalEntity {
    private static final long serialVersionUID = -5982916971163589494L;

    private String name;
    private Set<URI> types;
    private Collection<Study> studies;

    public SimpleBiologicalEntity(URI uri, String name) {
        super(uri);
        this.name = name;
        this.studies = new HashSet<>();
        this.types = new HashSet<URI>();
    }

    public SimpleBiologicalEntity(URI uri, String name, Study... studies) {
        super(uri);
        this.name = name;
        this.studies = new HashSet<>();
        this.types = new HashSet<URI>();
        Collections.addAll(this.studies, studies);
    }

    public SimpleBiologicalEntity(URI uri, String name, URI type, Study... studies) {
        super(uri);
        this.name = name;
        this.studies = new HashSet<>();
        this.types = new HashSet<URI>();
        if (type != null) {
            getTypes().add(type);
        }
        Collections.addAll(this.studies, studies);
    }

    public SimpleBiologicalEntity(URI uri, String name, Collection<URI> type, Study... studies) {
        super(uri);
        this.name = name;
        this.studies = new HashSet<>();
        this.types = new HashSet<URI>();
        getTypes().addAll(type);
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

    @Override
    public Set<URI> getTypes() {
        return types;
    }
}
