package uk.ac.ebi.spot.model;

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
public class SolrAnnotation implements SimpleAnnotation, Qualitative {

    @Id
    @Field
    private String id;

    @Score
    private Float score;

    @Field
    @Indexed
    private String propertyType;

    @Field
    @Indexed
    private String propertyValue;

    @Field
    private Collection<String> semanticTags;

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

    public Float getScore() {
        return score;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    public Collection<String> getSemanticTags() {
        return semanticTags;
    }

    @Override
    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setSemanticTags(Collection<String> semanticTags) {
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
