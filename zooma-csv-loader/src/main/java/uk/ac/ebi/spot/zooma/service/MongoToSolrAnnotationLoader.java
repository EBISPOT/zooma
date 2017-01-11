package uk.ac.ebi.spot.zooma.service;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.mongo.AnnotationRepository;
import uk.ac.ebi.spot.zooma.repository.SolrAnnotationRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Service
public class MongoToSolrAnnotationLoader implements AnnotationLoadingService {

    @Autowired
    AnnotationRepository mongoAnnotationRepository;

    @Autowired
    SolrAnnotationRepositoryService annotationSummaryRepositoryService;

    @Autowired
    SolrAnnotationRepository solrAnnotationRepository;

    Logger log = Logger.getLogger(this.getClass());

    @Override
    public void load() throws IOException {

        log.info("Retrieving all documents from mongodb starting...");
        List<uk.ac.ebi.spot.zooma.model.mongo.Annotation> mongoAnnotations = mongoAnnotationRepository.findAll();
        log.info("Retrieving all documents from mongodb finished, loaded: " + mongoAnnotations.size() + " annotations.");
        log.info("Loading into solr starting...");
        List<Annotation> solrAnnotations = new ArrayList<>();
        for(uk.ac.ebi.spot.zooma.model.mongo.Annotation mongoAnnotation : mongoAnnotations){

//            Property property = mongoAnnotation.getAnnotatedProperty();
//            String propertyType = "";
////            if (property instanceof TypedProperty){
//                propertyType = ((TypedProperty) property).getPropertyType();
////            }
//
//            String propertyValue = property.getPropertyValue();

            Annotation solrAnnotation = new Annotation(mongoAnnotation.getProperty().getPropertyType(),
                    mongoAnnotation.getProperty().getPropertyValue(),
                    mongoAnnotation.getSemanticTag(),
                    mongoAnnotation.getId(),
                    mongoAnnotation.getProvenance().getSource().getName(),
                    mongoAnnotation.getQuality());
            solrAnnotations.add(solrAnnotation);
        }
        int numDocuments = solrAnnotations.size();
        log.debug("Extracted " + numDocuments + " documents");

        // Index documents in batches
        int count = 0;
        while (count < numDocuments) {
            int end = count + 1000;
            if (end > numDocuments) {
                end = numDocuments;
            }
            solrAnnotationRepository.save(solrAnnotations.subList(count, end));
            count = end;
            log.debug("Indexed " + count + " / " + numDocuments +  "  entries");
        }
        log.info("Loading into solr finished...");
        log.info("Loaded: " + solrAnnotations.size() + " annotations into solr");
    }

    @Override
    public List returnLoaded() {
        return null;
    }
}
