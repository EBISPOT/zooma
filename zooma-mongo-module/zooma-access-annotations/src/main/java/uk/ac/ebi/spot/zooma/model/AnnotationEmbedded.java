package uk.ac.ebi.spot.zooma.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by olgavrou on 08/02/2017.
 */
public class AnnotationEmbedded {

    @JsonProperty("annotations")
    BaseAnnotation[] annotations;

    public BaseAnnotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(BaseAnnotation[] annotations) {
        this.annotations = annotations;
    }
}
