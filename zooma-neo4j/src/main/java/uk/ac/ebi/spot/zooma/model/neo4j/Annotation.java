package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Data
@NodeEntity
public class Annotation {

    @GraphId
    Long id;

    @Index
    String mongoid;

    @Relationship(type = "HAS_BIO_ENTITY")
    private BiologicalEntity biologicalEntity;

    @Relationship(type = "HAS_PROPERTY")
    private Property property;

    @Relationship(type = "HAS_SEMANTIC_TAG")

    private Collection<SemanticTag> semanticTag;

    @Relationship(type = "HAS_PROVENANCE")
    private Provenance provenance;

    @Relationship(type = "REPLACES")
    private Annotation replaces;

    private Float quality;

}
