package uk.ac.ebi.spot.zooma.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Created by olgavrou on 07/02/2017.
 */
@JsonIgnoreProperties
public class AnnotationResponse {

    @JsonProperty("_embedded")
    AnnotationEmbedded embeddedAnnotations;

    public AnnotationEmbedded getEmbeddedAnnotations() {
        return embeddedAnnotations;
    }

    public void setEmbeddedAnnotations(AnnotationEmbedded embeddedAnnotations) {
        this.embeddedAnnotations = embeddedAnnotations;
    }
}
