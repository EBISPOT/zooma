package uk.ac.ebi.spot;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.config.MongoConfig;
import uk.ac.ebi.spot.services.*;

import java.net.URI;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Created by olgavrou on 04/08/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class AnnotationRepositoryServiceIT {

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
    public void testFindAllDocuments(){
        List<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getAllDocuments();
        assertTrue("More than 0", mongoAnnotations.size() > 0);
    }

    @Test
    public void testGetBySemanticTags(){
        Collection<String> semanticTags = new HashSet<>();
        semanticTags.add("http://www.ebi.ac.uk/efo/EFO_test");
        Collection<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getBySemanticTags(semanticTags);
        assertTrue(mongoAnnotations.size() > 0);
    }

    @Test
    public void testGetByAnnotatedBiologicalEntitiesStudiesAccession(){
        Collection<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getByAnnotatedBiologicalEntitiesStudiesAccession("Accession1");
        for (Annotation annotation : mongoAnnotations){
            Collection<BiologicalEntity> biologicalEntity = annotation.getAnnotatedBiologicalEntities();
            for(BiologicalEntity entity : biologicalEntity){
                Collection<Study> studies = entity.getStudies();
                for (Study study : studies){
                    assertTrue(study.getAccession().equals("Accession1") || study.getAccession().equals("Accession2"));
                }
            }
        }
    }

    @Test
    public void testGetByAnnotatedBiologicalEntitiesName(){
        Collection<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getByAnnotatedBiologicalEntitiesName("GSMTest1");
        for(MongoAnnotation mongoAnnotation : mongoAnnotations){
            MongoTypedProperty simpleTypedProperty = (MongoTypedProperty) mongoAnnotation.getAnnotatedProperty();
            assertTrue((simpleTypedProperty.getPropertyType().equals("test type")));
            assertTrue(simpleTypedProperty.getPropertyValue().equals("test value"));
        }
    }

    @Test
    public void testGetByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(){
        Collection<MongoAnnotation> mongoAnnotations = mongoAnnotationRepositoryService.getByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession("GSMTest2", "Accession2");

        MongoAnnotation annotation = (MongoAnnotation) mongoAnnotations.toArray()[0];
        Collection<BiologicalEntity> biologicalEntities = annotation.getAnnotatedBiologicalEntities();
        for (BiologicalEntity biologicalEntity : biologicalEntities){
            assertTrue(biologicalEntity.getName().equals("GSMTest2") || biologicalEntity.getName().equals("GSMTest1"));
            Collection<Study> studies = biologicalEntity.getStudies();
            for (Study study : studies){
                assertTrue(study.getAccession().equals("Accession1") || study.getAccession().equals("Accession2"));
            }
        }
    }

    @Test
    public void testGetByProvenanceSource(){
        AnnotationSource source = new MongoOntologyAnnotationSource("http://www.ebi.ac.uk/test", "test", "", "", "");
        Collection<MongoAnnotation> annotations = mongoAnnotationRepositoryService.getByProvenanceSource(source, new PageRequest(0, 20));

        assertTrue(annotations.size() > 0);
    }


    @Test
    public void testGetByProvenanceSourceName(){
        Collection<MongoAnnotation> annotations = mongoAnnotationRepositoryService.getByProvenanceSourceName("test", new PageRequest(0, 20));
        assertTrue(annotations.size() > 0);
        MongoAnnotation annotation = (MongoAnnotation) annotations.toArray()[0];
        assertTrue(annotation.getProvenance().getSource().getName().equals("test"));
    }

    @Test
    public void testGetAllBiologicalEntities(){
        Collection<BiologicalEntity> biologicalEntities = mongoAnnotationRepositoryService.getAllBiologicalEntities();
        assertTrue(biologicalEntities.size() > 0);
    }

    @Test
    public void testGetAllStudies(){
        Collection<Study> studies = mongoAnnotationRepositoryService.getAllStudies();
        assertTrue(studies.size() > 0);
    }

    @Test
    public void testGetAllDocuments() throws Exception {
        Collection<MongoAnnotation> annotationDocumentList = mongoAnnotationRepositoryService.getAllDocuments();
        assertThat("Not empty list", annotationDocumentList.size(), is(not(0)));
    }

    @Test
    public void testGetByAnnotatedProperty() throws Exception {
        Collection<MongoAnnotation> annotationDocument = mongoAnnotationRepositoryService.getByAnnotatedPropertyValue("test value");

        MongoAnnotation annotation = (MongoAnnotation) annotationDocument.toArray()[0];
        assertThat("The property value should be \"test value\"", annotation.getAnnotatedProperty().getPropertyValue(), is("test value"));

        Property aProperty = new MongoTypedProperty("test type", "test value");
        annotationDocument = mongoAnnotationRepositoryService.getByAnnotatedProperty(aProperty);

        MongoAnnotation mongoAnnotation = (MongoAnnotation) annotationDocument.toArray()[0];
        assertThat("The property type should be \"test type\"", ((MongoTypedProperty) mongoAnnotation.getAnnotatedProperty()).getPropertyType(), is("test type"));
    }

    @Test
    public void testUpdate() throws Exception{
        Collection<MongoAnnotation> annotationDocument = mongoAnnotationRepositoryService.getByAnnotatedPropertyValue("test value");
        MongoAnnotation annotation = (MongoAnnotation) annotationDocument.toArray()[0];

        Property oldProperty = annotation.getAnnotatedProperty();
        String oldId = annotation.getId();

        annotation.setAnnotatedProperty(new MongoTypedProperty("new type", "new value"));

        MongoAnnotation updatedAnnotation = mongoAnnotationRepositoryService.update(annotation);

        assertTrue(updatedAnnotation.getId().equals(oldId));
        assertTrue(updatedAnnotation.getAnnotatedProperty().getPropertyValue().equals("new value"));

        updatedAnnotation.setAnnotatedProperty(oldProperty);
        mongoAnnotationRepositoryService.update(updatedAnnotation);

    }

}