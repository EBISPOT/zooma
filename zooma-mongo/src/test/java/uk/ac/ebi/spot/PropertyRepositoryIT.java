package uk.ac.ebi.spot;

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
 * Created by olgavrou on 15/09/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class PropertyRepositoryIT {

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

        Property property2 = new MongoUntypedProperty("test value 2");
        MongoAnnotation annotationDocument2 = new MongoAnnotation(biologicalEntities,
                property2,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        mongoAnnotationRepositoryService.save(annotationDocument2);

        Property property3 = new MongoTypedProperty("test type", "test value 2");
        MongoAnnotation annotationDocument3 = new MongoAnnotation(biologicalEntities,
                property3,
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
    public void testGetDistinctAnnotatedProperties(){
        List<Property> properties = mongoAnnotationRepositoryService.getAllProperties();
        assertTrue(properties.size() == 3);
    }

    @Test
    public void testGetAllPropertyTypes(){
        List<String> propTypes = mongoAnnotationRepositoryService.getAllPropertyTypes();
        assertTrue(propTypes.size() == 1);
    }

    @Test
    public void testGetPropertyFromPropertyType(){
        List<Property> properties = mongoAnnotationRepositoryService.getPropertiesByPropertyType("test type");
        assertTrue(properties.size() == 2);
    }

    @Test
    public void testGetPropertyFromPropertyValue(){
        List<Property> properties = mongoAnnotationRepositoryService.getPropertiesByPropertyValue("test value");
        assertTrue(properties.isEmpty());

        properties = mongoAnnotationRepositoryService.getPropertiesByPropertyValue("test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(properties.get(0).getClass().equals(MongoUntypedProperty.class));
    }

    @Test
    public void testGetPropertyFromPropertyTypeAndPropertyValue(){
//        Property property = new MongoTypedProperty("test type", "test value");
//        Property property2 = new MongoUntypedProperty("test value 2");
//        Property property3 = new MongoTypedProperty("test type", "test value 2");

        List<Property> properties = mongoAnnotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue(null, "test value");
        assertTrue(properties.size() == 1);
        MongoTypedProperty prop = (MongoTypedProperty) properties.get(0);
        assertTrue(prop.getPropertyType().equals("test type"));

        properties = mongoAnnotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue("test type", null);
        assertTrue(properties.size() == 2);
        for (Property property : properties){
            MongoProperty p = (MongoProperty) property;
            assertTrue(p.getPropertyValue().equals("test value") || p.getPropertyValue().equals("test value 2"));
        }

        properties = mongoAnnotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue("test type", "test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(((MongoTypedProperty) properties.get(0)).getPropertyType().equals("test type"));
        assertTrue((properties.get(0)).getPropertyValue().equals("test value 2"));

        properties = mongoAnnotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue(null, "test value 2");
        assertTrue(properties.size() == 2);

    }

}
