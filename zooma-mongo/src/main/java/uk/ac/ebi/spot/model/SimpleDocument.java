package uk.ac.ebi.spot.model;

import org.springframework.data.annotation.Id;

/**
 * Created by olgavrou on 04/08/2016.
 */
public abstract class SimpleDocument {

    @Id
    private String id;

    public SimpleDocument(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
