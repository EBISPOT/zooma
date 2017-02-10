package uk.ac.ebi.spot.zooma.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by olgavrou on 08/02/2017.
 */
public class AnnotationEmbedded {

    @JsonProperty("annotations")
    Annotation[] annotations;

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }
}
