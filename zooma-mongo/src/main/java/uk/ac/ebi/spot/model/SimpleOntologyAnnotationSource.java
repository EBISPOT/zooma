package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 05/08/2016.
 */
public class SimpleOntologyAnnotationSource extends SimpleAnnotationSource {

    private String title;
    private String description;

    public SimpleOntologyAnnotationSource(URI source, String name, String title, String description) {
        super(source, name, Type.ONTOLOGY);
        this.title = title;
        this.description = description;
    }

    public String getTitle() {if (title == null){title = "";} return title;}

    public String getDescription() {if (description == null){description = "No description";} return description;}

    @Override
    public String toString() {
        return "SimpleAnnotationSource{" +
                "source=" + getSource() +
                ", type=" + getType() +
                ", name='" + getName() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}
