package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.ac.ebi.spot.cascade.CascadeSave;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Document(collection = "biologicalEntities")
public class SimpleBiologicalEntity extends SimpleDocument implements BiologicalEntity {

    private String name;
    private Set<URI> types;
    @DBRef
    @CascadeSave
    private Collection<Study> studies;

    public SimpleBiologicalEntity(String name, Collection<URI> types, Collection<Study> studies) {
        this.name = name;
        this.studies = new HashSet<>();
        this.types = new HashSet<URI>();
        if (types != null) {
            getTypes().addAll(types);
        }
        if (studies != null) {
            this.studies.addAll(studies);
        }
    }

    @Override public String getName() {
        return name;
    }

    @Override public Collection<Study> getStudies() {
        return studies;
    }

    @Override
    public Set<URI> getTypes() {
        return types;
    }

    @Override
    public URI getURI() {
        return null;
    }
}
