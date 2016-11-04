package uk.ac.ebi.spot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;
import uk.ac.ebi.spot.services.SolrAnnotationRepositoryService;

import java.util.List;

/**
 * Created by olgavrou on 14/10/2016.
 */
@Service
public class SearchSolr {

    @Autowired
    SolrAnnotationRepositoryService annotationRepositoryService;

    @Autowired
    MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

    public List<AnnotationSummary> findByAnnotatedPropertyValue(String annotatedPropertyValue){
        return annotationRepositoryService.getAnnotationSummariesByPropertyValue(annotatedPropertyValue);
    }

    public MongoAnnotation getMongoAnnotationById(String mongoid){
        return mongoAnnotationRepositoryService.get(mongoid);
    }
}
