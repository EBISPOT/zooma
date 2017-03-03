package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.*;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data
@NodeEntity
public class AnnotationProvenance {

    Long id;

    @Index(unique=true,primary = true)
    private Source source;
    private String evidence;
    private String accuracy;
    private String generator;
    private String generatedDate;
    private String annotator;
    private String annotatedDate;

}
