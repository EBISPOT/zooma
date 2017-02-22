package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.*;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Data
@NodeEntity
public class SemanticTag {

    Long id;
    String semanticTag;
}
