package uk.ac.ebi.spot.zooma.model;

import lombok.*;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Data
@NodeEntity
public class AnnotationProvenance {

    Long id;

    private Source source;
    private String evidence;
    private String accuracy;
    private String generator;
    private String generatedDate;
    private String annotator;
    private String annotationDate;

}
