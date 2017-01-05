package uk.ac.ebi.spot;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by olgavrou on 14/09/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class StudyRepositoryServiceIT {

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


        MongoAnnotation annotationDocument2 = new MongoAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);

        mongoAnnotationRepositoryService.save(annotationDocument2);


        String semanticTag2 = "http://www.ebi.ac.uk/efo/EFO_test2";
        semanticTags.add(semanticTag2);


        MongoStudy mongoStudy1 = new MongoStudy("Accession3", null);
        Collection<Study> studies1 = new ArrayList<>();
        studies1.add(mongoStudy1);
        MongoStudy mongoStudy2 = new MongoStudy("Accession4", null);
        studies1.add(mongoStudy2);

        Collection<BiologicalEntity> biologicalEntities1 = new ArrayList<>();
        MongoBiologicalEntity biologicalEntity1 = new MongoBiologicalEntity("GSMTest1", studies1, null);
        biologicalEntities1.add(biologicalEntity1);

        MongoAnnotation annotationDocument3 = new MongoAnnotation(biologicalEntities1,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        mongoAnnotationRepositoryService.save(annotationDocument3);
    }

    @After
    public void teardown(){
        //remove the annotation from the database
        mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void testGetDistinctStudies(){
        Collection<Study> studyList = mongoAnnotationRepositoryService.getAllStudies();

        DBCollection collection = mongoTemplate.getCollection("annotations");
        List<DBObject> studies = collection.distinct("annotatedBiologicalEntities.studies");

        assertTrue(studies.size() == studyList.size());
    }

    @Test
    public void testGetBySemanticTags(){

        Collection<Study> studies = mongoAnnotationRepositoryService.getStudiesBySemanticTags("http://www.ebi.ac.uk/efo/EFO_test", "http://www.ebi.ac.uk/efo/EFO_test2");
        assertTrue(studies.size() == 2);

        studies = mongoAnnotationRepositoryService.getStudiesBySemanticTags("http://www.ebi.ac.uk/efo/EFO_test");
        assertTrue(studies.size() == 4);
    }

    @Test
    public void testGetByAccession(){
        Collection<Study> studies = mongoAnnotationRepositoryService.getStudiesByAccession("Accession1");
        assertTrue(studies.size() == 1);
        Study study = (Study) studies.toArray()[0];
        assertTrue(study.getAccession().equals("Accession1"));
    }
}
