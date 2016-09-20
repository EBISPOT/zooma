package uk.ac.ebi.spot.model;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by olgavrou on 04/08/2016.
 */
public class MongoBiologicalEntity implements BiologicalEntity {

    private String name;
    private Collection<Study> studies;
    private URI uri;


    public MongoBiologicalEntity(String name, Collection<Study> studies, URI uri) {
        this.name = name;
        this.uri = uri;
        this.studies = new HashSet<>();
        if (studies != null) {
            this.studies.addAll(studies);
        }
    }

    @Override public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public Collection<Study> getStudies() {
        return studies;
    }

    public void setStudies(Collection<Study> studies) {
        this.studies = studies;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
