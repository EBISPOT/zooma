package uk.ac.ebi.spot.model;

import java.util.Collection;

/**
 * Created by olgavrou on 31/10/2016.
 */
public class SolrAnnotationSummary implements AnnotationSummary {

    private String annotatedPropertyType;

    private String annotatedPropertyValue;

    private Collection<String> semanticTags;

    private String mongoid;

    private String source;

    private float quality;

    public SolrAnnotationSummary(String annotatedPropertyType, String annotatedPropertyValue, Collection<String> semanticTags, String mongoid, String source, float quality) {
        this.annotatedPropertyType = annotatedPropertyType;
        this.annotatedPropertyValue = annotatedPropertyValue;
        this.semanticTags = semanticTags;
        this.mongoid = mongoid;
        this.source = source;
        this.quality = quality;
    }

    @Override
    public String getAnnotatedPropertyType() {
        return annotatedPropertyType;
    }

    public void setAnnotatedPropertyType(String annotatedPropertyType) {
        this.annotatedPropertyType = annotatedPropertyType;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getAnnotatedPropertyValue() {
        return annotatedPropertyValue;
    }

    public void setAnnotatedPropertyValue(String annotatedPropertyValue) {
        this.annotatedPropertyValue = annotatedPropertyValue;
    }

    @Override
    public Collection<String> getSemanticTags() {
        return semanticTags;
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

    @Override
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }
}
