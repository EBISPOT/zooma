package uk.ac.ebi.spot.service;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.model.MongoTypedProperty;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;
import uk.ac.ebi.spot.services.SolrAnnotationRepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Service
public class Mongo2SolrLoader implements LoadService {

    @Autowired
    MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

    @Autowired
    SolrAnnotationRepositoryService annotationSummaryRepositoryService;

    Logger log = Logger.getLogger(this.getClass());

    @Override
    public void load() throws IOException {

        log.info("Retrieving all documents from mongodb starting...");
        List<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getAllDocuments();
        log.info("Retrieving all documents from mongodb finished, loaded: " + mongoAnnotations.size() + " annotations.");
        log.info("Loading into solr starting...");
        List<SolrAnnotation> solrAnnotations = new ArrayList<>();
        for(MongoAnnotation mongoAnnotation : mongoAnnotations){

            Property property = mongoAnnotation.getAnnotatedProperty();
            String propertyType = "";
            if (property instanceof MongoTypedProperty){
                propertyType = ((MongoTypedProperty) property).getPropertyType();
            }

            String propertyVale = property.getPropertyValue();

            SolrAnnotation solrAnnotation = new SolrAnnotation();
            solrAnnotation.setAnnotatedPropertyType(propertyType);
            solrAnnotation.setAnnotatedPropertyValue(propertyVale);
            solrAnnotation.setSemanticTags(mongoAnnotation.getSemanticTags());
            solrAnnotation.setMongoid(mongoAnnotation.getId());
            solrAnnotation.setId(mongoAnnotation.getId());
            solrAnnotation.setSource(mongoAnnotation.getProvenance().getSource().getName());
            solrAnnotation.setQuality(mongoAnnotation.getQuality());

            annotationSummaryRepositoryService.save(solrAnnotation);
            solrAnnotations.add(solrAnnotation);
        }
        log.info("Loading into solr finished...");
        log.info("Loaded: " + solrAnnotations.size() + " annotations into solr");
    }

    @Override
    public List returnLoaded() {
        return null;
    }
}
