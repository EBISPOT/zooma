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
import uk.ac.ebi.spot.services.AnnotationRepositoryService;

import java.net.URI;
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

        Property property2 = new SimpleUntypedProperty("test value 2");
        SimpleAnnotation annotationDocument2 = new SimpleAnnotation(biologicalEntities,
                property2,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        annotationRepositoryService.save(annotationDocument2);

        Property property3 = new SimpleTypedProperty("test type", "test value 2");
        SimpleAnnotation annotationDocument3 = new SimpleAnnotation(biologicalEntities,
                property3,
                semanticTags,
                annotationProvenance,
                null,
                null, false);
        annotationRepositoryService.save(annotationDocument3);
    }

    @After
    public void teardown(){
        //remove the annotation from the database
        mongoTemplate.getDb().dropDatabase();
    }

    @Test
    public void testGetDistinctAnnotatedProperties(){
        List<Property> properties = annotationRepositoryService.getAllProperties();
        assertTrue(properties.size() == 3);
    }

    @Test
    public void testGetAllPropertyTypes(){
        List<String> propTypes = annotationRepositoryService.getAllPropertyTypes();
        assertTrue(propTypes.size() == 1);
    }

    @Test
    public void testGetPropertyFromPropertyType(){
        List<Property> properties = annotationRepositoryService.getPropertiesByPropertyType("test type");
        assertTrue(properties.size() == 2);
    }

    @Test
    public void testGetPropertyFromPropertyValue(){
        List<Property> properties = annotationRepositoryService.getPropertiesByPropertyValue("test value");
        assertTrue(properties.isEmpty());

        properties = annotationRepositoryService.getPropertiesByPropertyValue("test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(properties.get(0).getClass().equals(SimpleUntypedProperty.class));
    }

    @Test
    public void testGetPropertyFromPropertyTypeAndPropertyValue(){
//        Property property = new SimpleTypedProperty("test type", "test value");
//        Property property2 = new SimpleUntypedProperty("test value 2");
//        Property property3 = new SimpleTypedProperty("test type", "test value 2");

        List<Property> properties = annotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue(null, "test value");
        assertTrue(properties.size() == 1);
        SimpleTypedProperty prop = (SimpleTypedProperty) properties.get(0);
        assertTrue(prop.getPropertyType().equals("test type"));

        properties = annotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue("test type", null);
        assertTrue(properties.size() == 2);
        for (Property property : properties){
            SimpleProperty p = (SimpleProperty) property;
            assertTrue(p.getPropertyValue().equals("test value") || p.getPropertyValue().equals("test value 2"));
        }

        properties = annotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue("test type", "test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(((SimpleTypedProperty) properties.get(0)).getPropertyType().equals("test type"));
        assertTrue((properties.get(0)).getPropertyValue().equals("test value 2"));

        properties = annotationRepositoryService.getPropertiesByPropertyTypeAndPropertyValue(null, "test value 2");
        assertTrue(properties.size() == 2);

    }

}
