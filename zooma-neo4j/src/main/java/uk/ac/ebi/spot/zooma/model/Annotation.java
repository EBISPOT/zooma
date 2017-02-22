package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Data
@NodeEntity
public class Annotation {

    Long id;

    @Relationship(type = "HAS_BIO_ENTITY")
    private BiologicalEntity biologicalEntities;

    @Relationship(type = "HAS_PROPERTY")
    private Property property;

    @Relationship(type = "HAS_SEMANTIC_TAG")
    private Collection<SemanticTag> semanticTag;

    @Relationship(type = "HAS_PROVENANCE")
    private AnnotationProvenance provenance;

    private boolean batchLoad;

    private Float quality;

}
