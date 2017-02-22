package uk.ac.ebi.spot.zooma.model.neo4j;


import lombok.*;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Created by olgavrou on 05/08/2016.
 */
@NodeEntity
@Data public class Study {

    Long id;

    private String study;
    private String studyUri;

}
