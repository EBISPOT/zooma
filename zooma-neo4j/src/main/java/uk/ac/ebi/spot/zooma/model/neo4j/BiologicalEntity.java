package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.*;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by olgavrou on 04/08/2016.
 */
@NodeEntity
@Data public class BiologicalEntity {

    Long id;

    private String bioEntity;

    @Relationship(type = "HAS_STUDY")
    private Study studies;

    private String bioEntityUri;

}
