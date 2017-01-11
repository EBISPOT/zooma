package uk.ac.ebi.spot.zooma.model;

import java.util.Collection;

/**
 * Created by olgavrou on 27/10/2016.
 */
public class SimpleAnnotationPrediction {

    private AnnotationPrediction.Confidence confidence;

    private String annotatedPropertyType;

    private String annotatedPropertyValue;

    private Collection<String> semanticTag;

    private String source;

    private float quality;

    public SimpleAnnotationPrediction() {
    }

    public SimpleAnnotationPrediction(String annotatedPropertyType, String annotatedPropertyValue, Collection<String> semanticTag, String source, float quality, AnnotationPrediction.Confidence confidence) {
        this.annotatedPropertyType = annotatedPropertyType;
        this.annotatedPropertyValue = annotatedPropertyValue;
        this.semanticTag = semanticTag;
        this.source = source;
        this.quality = quality;
        this.confidence = confidence;

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

    public Collection<String> getSemanticTag() {
        return semanticTag;
    }


    public void setSemanticTag(Collection<String> semanticTag) {
        this.semanticTag = semanticTag;
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
