package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.Property;

/**
 * Created by olgavrou on 10/03/2017.
 */
@Data
@RelationshipEntity(type = "HAS_PROVENANCE")
public class Provenance {

    @GraphId
    Long id;

    @StartNode
    private Annotation annotation;
    @EndNode
    private Source source;

    //Annotation Provenance Information goes here

    @Property
    private String evidence;
    @Property
    private String accuracy;
    @Property
    private String generator;
    @Property
    private String generatedDate;
    @Property
    private String annotator;
    @Property
    private String annotatedDate;

}