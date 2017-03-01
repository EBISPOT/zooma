package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.Data;
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

    Long id;

    String mongoId;

    @Relationship(type = "HAS_BIO_ENTITY")
    private BiologicalEntity biologicalEntity;

    @Relationship(type = "HAS_PROPERTY")
    private Property property;

    @Relationship(type = "HAS_SEMANTIC_TAG")
    @Index(unique=true,primary = true)
    private Collection<SemanticTag> semanticTag;

    @Relationship(type = "HAS_PROVENANCE")
    private AnnotationProvenance provenance;

    private boolean batchLoad;

    private Float quality;

}
