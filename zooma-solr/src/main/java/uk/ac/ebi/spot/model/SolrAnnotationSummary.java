//package uk.ac.ebi.spot.model;
//
//import org.apache.solr.client.solrj.beans.Field;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.solr.core.mapping.SolrDocument;
//
//import java.net.URI;
//import java.util.Collection;
//
///**
// * Created by olgavrou on 13/10/2016.
// */
//@SolrDocument(solrCoreName = "annotationsummaries")
//public class SolrAnnotationSummary implements AnnotationSummary{
//
//    @Id
//    @Field
//    String id;
//
//    @Field
//    private String annotatedPropertyType;
//    @Field
//    private String annotatedPropertyValue;
//    @Field
//    private Collection<URI> semanticTags;
//    @Field
//    private Collection<String> annotationIds;
//    @Field
//    private float quality;
//
//
//    @Override
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    @Override
//    public String getAnnotatedPropertyValue() {
//        return this.annotatedPropertyValue;
//    }
//
//    @Override
//    public String getAnnotatedPropertyType() {
//        return this.annotatedPropertyType;
//    }
//
//    @Override
//    public Collection<URI> getSemanticTags() {
//        return this.semanticTags;
//    }
//
//    @Override
//    public Collection<String> getAnnotationIds() {
//        return this.annotationIds;
//    }
//
//    @Override
//    public float getQuality() {
//        return this.quality;
//    }
//
//    public void setAnnotatedPropertyType(String annotatedPropertyType) {
//        this.annotatedPropertyType = annotatedPropertyType;
//    }
//
//    public void setAnnotatedPropertyValue(String annotatedPropertyValue) {
//        this.annotatedPropertyValue = annotatedPropertyValue;
//    }
//
//    public void setSemanticTags(Collection<URI> semanticTags) {
//        this.semanticTags = semanticTags;
//    }
//
//    public void setAnnotationIds(Collection<String> annotationIds) {
//        this.annotationIds = annotationIds;
//    }
//
//
//    public void setQuality(float quality) {
//        this.quality = quality;
//    }
//}
