package uk.ac.ebi.spot;

import com.mongodb.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.config.MongoConfig;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.services.AnnotationRepositoryService;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by olgavrou on 14/09/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class BiologicalEntityRepositoryServiceIT {

    @Autowired
    AnnotationRepositoryService annotationRepositoryService;


    @Autowired
    MongoTemplate mongoTemplate;

    @Before
    public void setup(){
        //Create an Annotation and store it in mongodb

        SimpleStudy simpleStudy = new SimpleStudy("Accession1", null);

        Collection<Study> studies = new ArrayList<>();
        studies.add(simpleStudy);

        simpleStudy = new SimpleStudy("Accession2", null);
        studies.add(simpleStudy);

        Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
        SimpleBiologicalEntity biologicalEntity = new SimpleBiologicalEntity("GSMTest1", studies, null);
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new SimpleBiologicalEntity("GSMTest2", studies, null);
        biologicalEntities.add(biologicalEntity);

        Property property = new SimpleTypedProperty("test type", "test value");
        URI semanticTag = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_test");
        Collection<URI> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        SimpleOntologyAnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/test"), "test","","");

        SimpleAnnotationProvenance annotationProvenance = new SimpleAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/test", new Date(), "Test annotator", new Date());

        SimpleAnnotation annotationDocument = new SimpleAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);

        annotationRepositoryService.save(annotationDocument);

        SimpleAnnotation annotation = new SimpleAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        annotationRepositoryService.save(annotation);
    }

    @After
    public void teardown(){
        //remove the annotations from the database
       mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void testGetDistinctBiologicalEntities(){
        Collection<BiologicalEntity> biologicalEntityList = annotationRepositoryService.getAllBiologicalEntities();

        DBCollection collection = mongoTemplate.getCollection("annotations");
        List<DBObject> biologicalEntities = collection.distinct("annotatedBiologicalEntities");


        assertTrue(biologicalEntityList.size() == biologicalEntities.size());
    }

    @Test
    public void testGetDistinctByStudyAccession(){
        Collection<BiologicalEntity> entities = annotationRepositoryService.getAllBiologicalEntitiesByStudyAccession("Accession1");

        assertTrue(entities.size() == 2);
    }

}
