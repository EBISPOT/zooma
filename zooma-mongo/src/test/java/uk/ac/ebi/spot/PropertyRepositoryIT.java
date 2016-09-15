package uk.ac.ebi.spot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.config.MongoConfig;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.services.AnnotationRepositoryService;
import uk.ac.ebi.spot.services.SimplePropertyService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNull;
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
    SimplePropertyService propertyService;

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

        Property property2 = new SimpleUntypedProperty("TestProperty2", "test value 2");
        SimpleAnnotation annotationDocument2 = new SimpleAnnotation("TestStringId2", biologicalEntities,
                property2,
                semanticTags,
                annotationProvenance,
                null,
                null);
        annotationRepositoryService.save(annotationDocument2);

        Property property3 = new SimpleTypedProperty("TestProperty3", "test type", "test value 2");
        SimpleAnnotation annotationDocument3 = new SimpleAnnotation("TestStringId3", biologicalEntities,
                property3,
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
    public void testGetDistinctAnnotatedProperties(){
        List<Property> properties = propertyService.getDistinctAnnotatedProperties();
        assertTrue(properties.size() == 3);
    }

    @Test
    public void testGetAllPropertyTypes(){
        List<String> propTypes = propertyService.getAllPropertyTypes();
        assertTrue(propTypes.size() == 1);
    }

    @Test
    public void testGetPropertyFromPropertyType(){
        List<Property> properties = propertyService.getPropertyFromPropertyType("test type");
        assertTrue(properties.size() == 2);
    }

    @Test
    public void testGetPropertyFromPropertyValue(){
        List<Property> properties = propertyService.getPropertyFromPropertyValue("test value");
        assertTrue(properties.isEmpty());

        properties = propertyService.getPropertyFromPropertyValue("test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(properties.get(0).getClass().equals(SimpleUntypedProperty.class));
    }

    @Test
    public void testGetPropertyFromPropertyTypeAndPropertyValue(){
//        Property property = new SimpleTypedProperty("TestProperty", "test type", "test value");
//        Property property2 = new SimpleUntypedProperty("TestProperty2", "test value 2");
//        Property property3 = new SimpleTypedProperty("TestProperty3", "test type", "test value 2");

        List<Property> properties = propertyService.getPropertyFromPropertyTypeAndPropertyValue(null, "test value");
        assertTrue(properties.size() == 1);
        SimpleTypedProperty prop = (SimpleTypedProperty) properties.get(0);
        assertTrue(prop.getId().equals("TestProperty"));

        properties = propertyService.getPropertyFromPropertyTypeAndPropertyValue("test type", null);
        assertTrue(properties.size() == 2);
        for (Property property : properties){
            SimpleProperty p = (SimpleProperty) property;
            assertTrue(p.getId().equals("TestProperty") || p.getId().equals("TestProperty3"));
        }

        properties = propertyService.getPropertyFromPropertyTypeAndPropertyValue("test type", "test value 2");
        assertTrue(properties.size() == 1);
        assertTrue(((SimpleTypedProperty) properties.get(0)).getId().equals("TestProperty3"));

        properties = propertyService.getPropertyFromPropertyTypeAndPropertyValue(null, "test value 2");
        assertTrue(properties.size() == 2);
        for (Property property : properties){
            SimpleProperty p = (SimpleProperty) property;
            assertTrue(p.getId().equals("TestProperty2") || p.getId().equals("TestProperty3"));
        }
    }

}
