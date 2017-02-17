package uk.ac.ebi.spot.zooma.model;

import lombok.*;

import java.util.Collection;

/**
 * Created by olgavrou on 31/10/2016.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Data public class AnnotationSummary implements Qualitative{

    private String propertyType;
    @NonNull
    private String propertyValue;
    @NonNull
    private Collection<String> semanticTags;
    @NonNull
    private String mongoid;
    @NonNull
    private Collection<String> source;
    @NonNull
    private float quality;

}
