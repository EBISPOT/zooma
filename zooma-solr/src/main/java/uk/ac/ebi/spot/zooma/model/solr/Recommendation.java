package uk.ac.ebi.spot.zooma.model.solr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;
import org.springframework.format.annotation.DateTimeFormat;
import uk.ac.ebi.spot.zooma.utils.SolrUtils;

import java.util.*;

/**
 * Created by Warren Read on 23/11/2018
 */
@SolrDocument(solrCoreName = "recommendations")
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recommendation {

    @Id
    @Field
    private String id;

    @Field
    private List<String> propertiesType;
    @Field
    private List<String> propertiesValue;
    @Field
    private String propertiesTypeTag;
    @Field
    private String propertiesValueTag;
    @Field
    private String tag;
    @Field
    private float conf;

    @Field
    private float support;

    @Field
    @Score
    private float lift;

    @Field
    private float conviction;

}
