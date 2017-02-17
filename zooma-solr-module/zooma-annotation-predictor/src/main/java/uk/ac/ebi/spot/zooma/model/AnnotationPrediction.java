package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NonNull;

import java.util.Collection;

/**
 * Created by olgavrou on 27/10/2016.
 */
@Data
public class AnnotationPrediction {

    @NonNull
    private String annotatedPropertyType;
    @NonNull
    private String annotatedPropertyValue;
    @NonNull
    private Collection<String> semanticTag;
    @NonNull
    private Collection<String> source;
    @NonNull
    private float quality;
    @NonNull
    private Confidence confidence;
    @NonNull
    String derivedFromMongoId;

    public enum Confidence {
        HIGH,
        GOOD,
        MEDIUM,
        LOW
    }
}
