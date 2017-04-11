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
 * Created by olgavrou on 13/10/2016.
 */
@SolrDocument(solrCoreName = "annotations")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotation implements Qualitative{

    @Id
    @Field
    private String id;

    @Score
    @Field
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
    private Collection<String> mongoid;

    @Field
    @NonNull
    private String strongestMongoid;

    @Field
    @NonNull
    private Collection<String> source;

    @Field
    @NonNull
    private float quality;

    @Field
    @NonNull
    private int votes;

    @Field
    @NonNull
    private int sourceNum;

    @Field
    @NonNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date lastModified;


    public boolean equals(Annotation summary){
        if(!summary.getPropertyValue().equals(this.getPropertyValue())){
            return false;
        }

        if(!summary.getPropertyType().equals(this.getPropertyType())){
            return false;
        }

        if(summary.getSemanticTag().size() != this.getSemanticTag().size()){
            return false;
        }

        if(!SolrUtils.listEqualsNoOrder(this.getSemanticTag(), summary.getSemanticTag())){ //if they are not equal
            return false;
        }

        return true;
    }
}
