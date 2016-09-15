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
import uk.ac.ebi.spot.services.AnnotationRepositoryService;
import uk.ac.ebi.spot.services.StudyRepositoryService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by olgavrou on 14/09/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class StudyRepositoryServiceIT {

    @Autowired
    AnnotationRepositoryService annotationRepositoryService;

    @Autowired
    StudyRepositoryService studyRepositoryService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Before
    public void setup(){
        //Create an Annotation and store it in mongodb

        SimpleStudy simpleStudy = new SimpleStudy("SS1", "Accession1");

        Collection<Study> studies = new ArrayList<>();
        studies.add(simpleStudy);

        simpleStudy = new SimpleStudy("SS2", "Accession2");
        studies.add(simpleStudy);

        Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
        SimpleBiologicalEntity biologicalEntity = new SimpleBiologicalEntity("BE1", "GSMTest1", studies);
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new SimpleBiologicalEntity("BE2", "GSMTest2", studies);
        biologicalEntities.add(biologicalEntity);

        Property property = new SimpleTypedProperty("TestProperty", "test type", "test value");
        URI semanticTag = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_test");
        Collection<URI> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        SimpleOntologyAnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/test"), "test","","");

        SimpleAnnotationProvenance annotationProvenance = new SimpleAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/test", new Date(), "Test annotator", new Date());

        SimpleAnnotation annotationDocument = new SimpleAnnotation("TestStringId", biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);

        annotationRepositoryService.save(annotationDocument);


        SimpleAnnotation annotationDocument2 = new SimpleAnnotation("TestStringId2", biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);

        annotationRepositoryService.save(annotationDocument2);


        URI semanticTag2 = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_test2");
        semanticTags.add(semanticTag2);


        SimpleStudy simpleStudy1 = new SimpleStudy("SS3", "Accession3");
        Collection<Study> studies1 = new ArrayList<>();
        studies1.add(simpleStudy1);
        SimpleStudy simpleStudy2 = new SimpleStudy("SS4", "Accession4");
        studies1.add(simpleStudy2);

        Collection<BiologicalEntity> biologicalEntities1 = new ArrayList<>();
        SimpleBiologicalEntity biologicalEntity1 = new SimpleBiologicalEntity("BE1", "GSMTest1", studies1);
        biologicalEntities1.add(biologicalEntity1);

        SimpleAnnotation annotationDocument3 = new SimpleAnnotation("TestStringId3", biologicalEntities1,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);
        annotationRepositoryService.save(annotationDocument3);
    }

    @After
    public void teardown(){
        //remove the annotation from the database
        SimpleAnnotation annotationDocument = annotationRepositoryService.get("TestStringId");
        annotationRepositoryService.delete(annotationDocument);
        assertNull(annotationRepositoryService.get(annotationDocument.getId()));

        annotationDocument = annotationRepositoryService.get("TestStringId2");
        annotationRepositoryService.delete(annotationDocument);
        assertNull(annotationRepositoryService.get(annotationDocument.getId()));

        annotationDocument = annotationRepositoryService.get("TestStringId3");
        annotationRepositoryService.delete(annotationDocument);
        assertNull(annotationRepositoryService.get(annotationDocument.getId()));
    }

    @Test
    public void testGetDistinctStudies(){
        Collection<Study> studyList = studyRepositoryService.getDistinctStudies();

        DBCollection collection = mongoTemplate.getCollection("annotations");
        List<DBObject> studies = collection.distinct("annotatedBiologicalEntities.studies");

        assertTrue(studies.size() == studyList.size());
    }

    @Test
    public void testGetBySemanticTags(){

        Collection<Study> studies = studyRepositoryService.getBySemanticTags(URI.create("http://www.ebi.ac.uk/efo/EFO_test"), URI.create("http://www.ebi.ac.uk/efo/EFO_test2"));
        assertTrue(studies.size() == 2);

        studies = studyRepositoryService.getBySemanticTags(URI.create("http://www.ebi.ac.uk/efo/EFO_test"));
        assertTrue(studies.size() == 4);
    }

    @Test
    public void testGetByAccession(){
        Collection<Study> studies = studyRepositoryService.getByAccession("Accession1");
        assertTrue(studies.size() == 1);
        Study study = (Study) studies.toArray()[0];
        assertTrue(study.getAccession().equals("Accession1"));
    }
}
