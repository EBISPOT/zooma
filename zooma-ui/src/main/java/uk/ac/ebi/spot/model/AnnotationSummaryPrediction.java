package uk.ac.ebi.spot.model;

import java.util.Collection;

/**
 * Created by olgavrou on 27/10/2016.
 */
public class AnnotationSummaryPrediction implements AnnotationSummary{

    private String id;


    private AnnotationPrediction.Confidence confidence;

    private String annotatedPropertyType;

    private String annotatedPropertyValue;

    private Collection<String> semanticTags;

    private String source;

    private float quality;

    public AnnotationSummaryPrediction() {
    }

    public AnnotationSummaryPrediction(String annotatedPropertyType, String annotatedPropertyValue, Collection<String> semanticTags, String source, float quality, AnnotationPrediction.Confidence confidence) {
        this.annotatedPropertyType = annotatedPropertyType;
        this.annotatedPropertyValue = annotatedPropertyValue;
        this.semanticTags = semanticTags;
        this.source = source;
        this.quality = quality;
        this.confidence = confidence;

    }

    public String getId() {
        return id;
    }

    public AnnotationPrediction.Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(AnnotationPrediction.Confidence confidence) {
        this.confidence = confidence;
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

    public Collection<String> getSemanticTags() {
        return semanticTags;
    }


    public void setSemanticTags(Collection<String> semanticTags) {
        this.semanticTags = semanticTags;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

}
