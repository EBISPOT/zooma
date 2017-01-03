package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 05/08/2016.
 */
public class MongoOntologyAnnotationSource extends MongoAnnotationSource {

    private String title;
    private String description;

    public MongoOntologyAnnotationSource(String uri, String name, String topic, String title, String description) {
        super(uri, name, Type.ONTOLOGY, topic);
        this.title = title;
        this.description = description;
    }

    public String getTitle() {if (title == null){title = "";} return title;}

    public String getDescription() {if (description == null){description = "No description";} return description;}

    @Override
    public String toString() {
        return "MongoAnnotationSourceRepository{" +
                "uri=" + getUri() +
                ", type=" + getType() +
                ", name='" + getName() + '\'' +
                ", topic='" + getTopic() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
