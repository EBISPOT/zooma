package uk.ac.ebi.spot.zooma.model.solr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;

import java.util.*;

/**
 * Created by olgavrou on 13/10/2016.
 */
@SolrDocument(solrCoreName = "annotations")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationSummary {

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


    public boolean equals(AnnotationSummary summary){
        if(!summary.getPropertyValue().equals(this.getPropertyValue())){
            return false;
        }

        if(!summary.getPropertyType().equals(this.getPropertyType())){
            return false;
        }

        if(summary.getSemanticTag().size() != this.getSemanticTag().size()){
            return false;
        }

        if(!listEqualsNoOrder(this.getSemanticTag(), summary.getSemanticTag())){ //if they are not equal
            return false;
        }

        return true;
    }

    public static <T> boolean listEqualsNoOrder(Collection<T> l1, Collection<T> l2) {
        final Set<T> s1 = new HashSet<>(l1);
        final Set<T> s2 = new HashSet<>(l2);

        return s1.equals(s2);
    }
}
