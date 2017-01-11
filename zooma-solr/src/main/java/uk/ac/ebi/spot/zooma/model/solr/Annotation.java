package uk.ac.ebi.spot.zooma.model.solr;

import lombok.Data;
import lombok.NonNull;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;

import java.util.Collection;

/**
 * Created by olgavrou on 13/10/2016.
 */
@SolrDocument(solrCoreName = "annotations")
@Data
public class Annotation {

    @Id
    @Field
    private String id;

    @Score
    private Float score;

    @Field
    @Indexed
    @NonNull
    private String propertyType;

    @Field
    @Indexed
    @NonNull
    private String propertyValue;

    @Field
    @NonNull
    private Collection<String> semanticTag;

    @Field
    @NonNull
    private String mongoid;

    @Field
    @NonNull
    private String source;

    @Field
    @NonNull
    private float quality;

}
