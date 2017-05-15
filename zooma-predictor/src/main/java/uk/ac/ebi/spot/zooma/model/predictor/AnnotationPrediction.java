package uk.ac.ebi.spot.zooma.model.predictor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.ResourceSupport;

import java.util.Collection;
import java.util.Date;

/**
 * Created by olgavrou on 27/10/2016.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationPrediction extends ResourceSupport implements Scorable {

    @NonNull
    private String propertyType;
    @NonNull
    private String propertyValue;
    @NonNull
    private Collection<String> semanticTag;
    @NonNull
    private Collection<String> mongoid;
    @NonNull
    private String strongestMongoid;
    @NonNull
    private Collection<String> source;
    @NonNull
    private float quality;
    @NonNull
    private int votes;
    @NonNull
    private int sourceNum;
    @NonNull
    private float score;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date lastModified;

    private Confidence confidence;

    @JsonIgnore
    public float getQuality(){
        return this.quality;
    }

    @JsonIgnore
    public int getSourceNum(){
        return sourceNum;
    }

    @JsonIgnore
    public int getVotes(){
        return votes;
    }

    @JsonIgnore
    public String getStrongestMongoid(){
        return strongestMongoid;
    }

    @JsonIgnore
    public Collection<String> getMongoid(){
        return mongoid;
    }

    public enum Confidence {
        HIGH,
        GOOD,
        MEDIUM,
        LOW
    }
}
