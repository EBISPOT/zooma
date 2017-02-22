package uk.ac.ebi.spot.zooma.model;

import lombok.*;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import uk.ac.ebi.spot.zooma.model.api.AnnotationSource;

/**
 * Created by olgavrou on 05/08/2016.
 */
@NodeEntity
@Data public class Source {

    Long id;

    private String uri;
    private String name;
    private String type;
    private String topic;

}
