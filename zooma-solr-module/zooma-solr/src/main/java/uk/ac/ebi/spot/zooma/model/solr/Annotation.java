package uk.ac.ebi.spot.zooma.model.solr;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;
import uk.ac.ebi.spot.zooma.model.SolrBaseAnnotation;

import java.util.*;

/**
 * Created by olgavrou on 13/10/2016.
 */
@SolrDocument(solrCoreName = "annotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Annotation {

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
    private String mongoid;

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

    public boolean equals(Annotation annotation){
        if(!annotation.getPropertyValue().equals(this.getPropertyValue())){
            return false;
        }

        if(!annotation.getPropertyType().equals(this.getPropertyType())){
            return false;
        }

        if(annotation.getSemanticTag().size() != this.getSemanticTag().size()){
            return false;
        }

        if(!listEqualsNoOrder(this.getSemanticTag(), annotation.getSemanticTag())){ //if they are not equal
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
