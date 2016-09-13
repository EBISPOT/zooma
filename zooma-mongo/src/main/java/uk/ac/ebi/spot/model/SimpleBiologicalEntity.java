package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Document(collection = "biologicalEntities")
public class SimpleBiologicalEntity extends SimpleDocument implements BiologicalEntity {

    private String name;
    private Collection<Study> studies;

    public SimpleBiologicalEntity(String id, String name, Collection<Study> studies) {
        super(id);
        this.name = name;
        this.studies = new HashSet<>();
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

}
