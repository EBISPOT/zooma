package uk.ac.ebi.spot.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;

import java.net.URI;
import java.util.Collection;

/**
 * Created by olgavrou on 13/10/2016.
 */
@SolrDocument(solrCoreName = "annotations")
public class SolrAnnotation implements AnnotationSummary{

    @Id
    @Field
    private String id;

    @Score
    private Float score;

    @Field
    private String annotatedPropertyType;

    @Field
    @Indexed
    private String annotatedPropertyValue;

    @Field
    private Collection<URI> semanticTags;

    @Field
    private String mongoid;

    @Field
    private String source;

    @Field
    private float quality;

//    public SolrAnnotation(String annotatedPropertyType, String annotatedPropertyValue, Collection<URI> semanticTags, String mongoid) {
//        this.annotatedPropertyType = annotatedPropertyType;
//        this.annotatedPropertyValue = annotatedPropertyValue;
//        this.semanticTags = semanticTags;
//        this.mongoid = mongoid;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getAnnotatedPropertyType() {
        return annotatedPropertyType;
    }

    public void setAnnotatedPropertyType(String annotatedPropertyType) {
        this.annotatedPropertyType = annotatedPropertyType;
    }

    public String getAnnotatedPropertyValue() {
        return annotatedPropertyValue;
    }

    public void setAnnotatedPropertyValue(String annotatedPropertyValue) {
        this.annotatedPropertyValue = annotatedPropertyValue;
    }

    public Collection<URI> getSemanticTags() {
        return semanticTags;
    }

    @Override
    public Collection<String> getAnnotationIds() {
        return null;
    }

    @Override
    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setSemanticTags(Collection<URI> semanticTags) {
        this.semanticTags = semanticTags;
    }

    public String getMongoid() {
        return mongoid;
    }

    public void setMongoid(String mongoid) {
        this.mongoid = mongoid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
