package uk.ac.ebi.spot.zooma.model.solr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.Score;
import org.springframework.format.annotation.DateTimeFormat;
import uk.ac.ebi.spot.zooma.utils.SolrUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Warren Read on 23/11/2018
 */
@SolrDocument(solrCoreName = "recommendations")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recommendation {

    @Id
    @Field
    private String id;

    @Field
    private List<String> propertiesType;
    @Field
    private List<String> propertiesValue;
    @Field
    private String propertiesTypeTag;
    @Field
    private String propertiesValueTag;
    @Field
    private String tag;
    @Field
    private float conf;

    @Field
    private float support;

    @Field
    private float lift;

    @Field
    private float conviction;

    public Recommendation() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPropertiesType() {
        return propertiesType;
    }

    public void setPropertiesType(List<String> propertiesType) {
        this.propertiesType = propertiesType;
    }

    public List<String> getPropertiesValue() {
        return propertiesValue;
    }

    public void setPropertiesValue(List<String> propertiesValue) {
        this.propertiesValue = propertiesValue;
    }

    public String getPropertiesTypeTag() {
        return propertiesTypeTag;
    }

    public void setPropertiesTypeTag(String propertiesTypeTag) {
        this.propertiesTypeTag = propertiesTypeTag;
    }

    public String getPropertiesValueTag() {
        return propertiesValueTag;
    }

    public void setPropertiesValueTag(String propertiesValueTag) {
        this.propertiesValueTag = propertiesValueTag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public float getConf() {
        return conf;
    }

    public void setConf(float conf) {
        this.conf = conf;
    }

    public float getSupport() {
        return support;
    }

    public void setSupport(float support) {
        this.support = support;
    }

    public float getLift() {
        return lift;
    }

    public void setLift(float lift) {
        this.lift = lift;
    }

    public float getConviction() {
        return conviction;
    }

    public void setConviction(float conviction) {
        this.conviction = conviction;
    }
}
