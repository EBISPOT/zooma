package uk.ac.ebi.spot.zooma.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class SolrBaseAnnotation {

    @NonNull
    private Float score;

    @NonNull
    private String propertyType;

    @NonNull
    private String propertyValue;

    @NonNull
    private Collection<String> semanticTag;

    @NonNull
    private String mongoid;

    @NonNull
    private Collection<String> source;

    @NonNull
    private float quality;

}
