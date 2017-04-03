package uk.ac.ebi.spot.zooma.model.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Data
@NoArgsConstructor
@Document(collection = "annotations")
public class Annotation {
    @Id
    @CreatedBy
    private String id;
    private String mongoid;
    @NonNull
    private BiologicalEntity biologicalEntities;
    @NonNull
    private Property property;
    @NonNull
    @Indexed
    private Collection<String> semanticTag;
    @NonNull
    private MongoAnnotationProvenance provenance;
    private Double quality;
    @NonNull
    @Indexed
    @JsonIgnore
    private String checksum;
    private Collection<String> replacedBy;
    private String replaces;


    public double getQuality() {
        if (this.quality == null){
            this.quality = calculateAnnotationQuality();
        }
        return quality;
    }

    public Collection<String> getReplacedBy(){
        if (replacedBy == null){
            this.replacedBy = new ArrayList<>();
        }
        return this.replacedBy;
    }


    public Annotation(BiologicalEntity biologicalEntity, Property property, Collection<String> semanticTag,
               MongoAnnotationProvenance annotationProvenance, String checksum){
        this.biologicalEntities = biologicalEntity;
        this.property = property;
        this.semanticTag = semanticTag;
        this.provenance = annotationProvenance;
        setQuality();
        this.provenance.setGeneratedDate(LocalDateTime.now());
        this.checksum = checksum;
    }

    /**
     * A field that indicates the annotationid of the document.
     * Not saved but populated on get to be exposed in the api
     * @return the id
     */
    public String getMongoid(){
        return id;
    }

    /**
     * Quality can not be set manually.
     * It can only calculated based on the annotation's {@link AnnotationProvenance}
     */
    public void setQuality(Double quality){
        this.quality = calculateAnnotationQuality();
    }

    public void setQuality(){
        this.quality = calculateAnnotationQuality();
    }

    /**
     * quality is caluclated based on provenance.
     * So we are making sure that it will be calculated once the provenance is set
     */
    public void setProvenance(MongoAnnotationProvenance provenance){
        this.provenance = provenance;
        this.provenance.setGeneratedDate(LocalDateTime.now());
        this.quality = calculateAnnotationQuality();
    }

    /**
     * Returns a float value that is the quality score for the given annotation.
     * <p/>
     * This score is evaluated by an algorithm that considers: <ul> <li>Source (e.g. Atlas, AE2, ZOOMA)</li>
     * <li>Evidence (Manually created, Inferred, etc.)</li> <li>Creator - Who made this annotation?</li> <li>Time of
     * creation - How recent is this annotation?</li> </ul>
     */
    private double calculateAnnotationQuality() throws IllegalArgumentException {

        if (this.provenance == null){
            throw new IllegalArgumentException("Provenance isn't set yet, can not calculate annotation provenance");
        }
        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = MongoAnnotationProvenance.Evidence.values().length - this.provenance.getEvidence().ordinal();
        // creation time should then work backwards from most recent to oldest
        long age = this.provenance.getAnnotatedDate().toLocalTime().toNanoOfDay();

        return (float) (evidenceScore + Math.log10(age));

    }

    @Override
    public String toString() {

        StringJoiner semTags = new StringJoiner(",");
        for(String s : getSemanticTag()){
            semTags.add("\"" + s + "\"");
        }

        String string = "{" +
                "\"provenance\" : {" +
                "\"evidence\" : \"" + getProvenance().getEvidence().toString() + "\"," +
                "\"annotatedDate\" : \"" + getProvenance().getAnnotatedDate().toString() + "\"," +
                "\"generatedDate\" : \"" + getProvenance().getGeneratedDate().toString() + "\"," +
                "\"accuracy\" : \"" + getProvenance().getAccuracy().toString() + "\"," +
                "\"generator\" : \"ZOOMA" + "\"," +
                "\"source\" : { " +
                "\"name\" : \"" + getProvenance().getSource().getName().toString() + "\"," +
                "\"topic\" : \"" + getProvenance().getSource().getTopic().toString() + "\"," +
                "\"type\" : \"" + getProvenance().getSource().getType() + "\"," +
                "\"uri\" : \"" + getProvenance().getSource().getUri() + "\"" +
                "}," +
                "\"annotator\" : \"" + getProvenance().getAnnotator() + "\"" +
                "}," +
                "\"biologicalEntities\" : {" +
                "\"studies\" : {" +
                "\"study\" : \"" + getBiologicalEntities().getStudies().getStudy() + "\"" +
                "}," +
                "\"bioEntity\" : \"" + getBiologicalEntities().getBioEntity() + "\"" +
                "}," +
                "\"semanticTag\" : [" + semTags.toString() + "]," +
                "\"quality\" : \"" + getQuality() + "\"," +
                "\"property\" : {" +
                "\"propertyType\" : \"" + getProperty().getPropertyType() + "\"," +
                "\"propertyValue\" : \"" + getProperty().getPropertyValue() + "\"" +
                "}" +
                "}";

        return string;
    }

    /**
     * Makes the annotation into a simple map
     *
     * @return all the properties of the annotation in a simple map
     */
    public Map<String, Object> toSimpleMap(){
        Map<String, Object> map = new HashMap<>();

        map.put("id", getId());
        map.put("quality", getQuality());
        map.put("propertyType", getProperty().getPropertyType());
        map.put("propertyValue", getProperty().getPropertyValue());
        map.put("semanticTag", getSemanticTag());
        map.put("bioEntity", getBiologicalEntities().getBioEntity());
        map.put("study", getBiologicalEntities().getStudies().getStudy());
        map.put("sourceName", getProvenance().getSource().getName());
        map.put("sourceTopic", getProvenance().getSource().getTopic());
        map.put("sourceType", getProvenance().getSource().getType());
        map.put("sourceUri", getProvenance().getSource().getUri());
        map.put("accuracy", getProvenance().getAccuracy());
        map.put("evidence", getProvenance().getEvidence());
        map.put("annotatedDate", getProvenance().getAnnotatedDate());
        map.put("generatedDate", getProvenance().getGeneratedDate());
        map.put("generator", getProvenance().getGenerator());
        map.put("annotator", getProvenance().getAnnotator());
        map.put("replaces", getReplaces());
        map.put("replacedBy", getReplacedBy());

        return map;
    }
}
