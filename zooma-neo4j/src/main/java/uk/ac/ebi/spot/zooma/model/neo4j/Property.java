package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.*;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by olgavrou on 03/08/2016.
 */
@NodeEntity(label = "Property")
@Data public class Property {

    Long id;

    private String propertyType;
    private String propertyValue;

    @Relationship(type = "ANNOTATES")
    BiologicalEntity biologicalEntity;
}
