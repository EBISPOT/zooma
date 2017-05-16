package uk.ac.ebi.spot.zooma.model.neo4j;

import lombok.*;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Collection;

/**
 * Created by olgavrou on 05/08/2016.
 */
@NodeEntity
@Data public class Source {

    Long id;

    @Index(unique=true,primary = true)
    private String uri;
    private String name;
    private String type;
    private Collection<String> topic;

}
