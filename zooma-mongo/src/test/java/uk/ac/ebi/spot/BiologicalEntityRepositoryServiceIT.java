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
import uk.ac.ebi.spot.services.BiologicalEntityRepositoryService;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertNull;
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
    BiologicalEntityRepositoryService biologicalEntityRepositoryService;

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

        SimpleAnnotation annotation = new SimpleAnnotation("TestStringId2", biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);
        annotationRepositoryService.save(annotation);
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
    }

    @Test
    public void testGetDistinctBiologicalEntities(){
        Collection<BiologicalEntity> biologicalEntityList = biologicalEntityRepositoryService.getDistinctBiologicalEntities();

        DBCollection collection = mongoTemplate.getCollection("annotations");
        List<DBObject> biologicalEntities = collection.distinct("annotatedBiologicalEntities");


        assertTrue(biologicalEntityList.size() == biologicalEntities.size());
    }

    @Test
    public void testGetDistinctByStudyAccession(){
        Collection<BiologicalEntity> entities = biologicalEntityRepositoryService.getDistinctByStudyAccession("Accession1");

        assertTrue(entities.size() == 2);
    }

}
