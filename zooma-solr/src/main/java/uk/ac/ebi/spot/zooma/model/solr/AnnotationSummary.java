package uk.ac.ebi.spot.zooma.model.solr;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.spot.zooma.model.Identifiable;
import uk.ac.ebi.spot.zooma.model.Qualitative;

import java.util.Collection;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Data public class AnnotationSummary implements Qualitative {

    private String annotatedPropertyType;
    @NonNull
    private String annotatedPropertyValue;
    @NonNull
    private Collection<String> semanticTags;
    @NonNull
    private String mongoid;
    @NonNull
    private String source;
    @NonNull
    private float quality;

    public AnnotationSummary(String annotatedPropertyType, String annotatedPropertyValue, Collection<String> semanticTags, String mongoid, String source, float quality) {
        this.annotatedPropertyType = annotatedPropertyType;
        this.annotatedPropertyValue = annotatedPropertyValue;
        this.semanticTags = semanticTags;
        this.mongoid = mongoid;
        this.source = source;
        this.quality = quality;
    }
}
