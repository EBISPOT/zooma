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
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;

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
    MongoAnnotationRepositoryService mongoAnnotationRepositoryService;


    @Autowired
    MongoTemplate mongoTemplate;

    @Before
    public void setup(){
        //Create an Annotation and store it in mongodb

        MongoStudy mongoStudy = new MongoStudy("Accession1", null);

        Collection<Study> studies = new ArrayList<>();
        studies.add(mongoStudy);

        mongoStudy = new MongoStudy("Accession2", null);
        studies.add(mongoStudy);

        Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
        MongoBiologicalEntity biologicalEntity = new MongoBiologicalEntity("GSMTest1", studies, null);
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new MongoBiologicalEntity("GSMTest2", studies, null);
        biologicalEntities.add(biologicalEntity);

        Property property = new MongoTypedProperty("test type", "test value");
        String semanticTag = "http://www.ebi.ac.uk/efo/EFO_test";
        Collection<String> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        MongoOntologyAnnotationSource annotationSource = new MongoOntologyAnnotationSource("http://www.ebi.ac.uk/test", "test","","", "");

        MongoAnnotationProvenance annotationProvenance = new MongoAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/test", new Date(), "Test annotator", new Date());

        MongoAnnotation annotationDocument = new MongoAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);

        mongoAnnotationRepositoryService.save(annotationDocument);

        MongoAnnotation annotation = new MongoAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        mongoAnnotationRepositoryService.save(annotation);
    }

    @After
    public void teardown(){
        //remove the annotations from the database
       mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void testGetDistinctBiologicalEntities(){
        Collection<BiologicalEntity> biologicalEntityList = mongoAnnotationRepositoryService.getAllBiologicalEntities();

        DBCollection collection = mongoTemplate.getCollection("annotations");
        List<DBObject> biologicalEntities = collection.distinct("annotatedBiologicalEntities");


        assertTrue(biologicalEntityList.size() == biologicalEntities.size());
    }

    @Test
    public void testGetDistinctByStudyAccession(){
        Collection<BiologicalEntity> entities = mongoAnnotationRepositoryService.getAllBiologicalEntitiesByStudyAccession("Accession1");

        assertTrue(entities.size() == 2);
    }

}
