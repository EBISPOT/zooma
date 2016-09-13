package uk.ac.ebi.spot;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
    AnnotationRepositoryService annotationRepositoryService;

    @Autowired
    SimplePropertyService propertyService;

    @Autowired
    BiologicalEntityRepositoryService biologicalEntityRepositoryService;

    @Autowired
    StudyRepositoryService studyRepositoryService;

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
    }

//    @After
//    public void teardown(){
//
//        //remove the annotation from the database
//        SimpleAnnotation annotationDocument = annotationRepositoryService.get("TestStringId");
//
//        annotationRepositoryService.delete(annotationDocument);
//
//        assertNull(annotationRepositoryService.get(annotationDocument.getId()));
//    }

    @Test
    public void testGetBySemanticTags(){
        Collection<URI> semanticTags = new HashSet<>();
        semanticTags.add(URI.create("http://www.ebi.ac.uk/efo/EFO_test"));
        List<SimpleAnnotation> simpleAnnotations = annotationRepositoryService.getBySemanticTags(semanticTags);
        assertTrue(simpleAnnotations.size() > 0);
    }

    @Test
    public void testGetByAnnotatedBiologicalEntitiesStudiesAccession(){
        List<SimpleAnnotation> simpleAnnotations = annotationRepositoryService.getByAnnotatedBiologicalEntitiesStudiesAccession("Accession1");
        for (Annotation annotation : simpleAnnotations){
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
        List<SimpleAnnotation> simpleAnnotations = annotationRepositoryService.getByAnnotatedBiologicalEntitiesName("GSMTest1");
        for(SimpleAnnotation simpleAnnotation : simpleAnnotations){
            SimpleTypedProperty simpleTypedProperty = (SimpleTypedProperty) simpleAnnotation.getAnnotatedProperty();
            assertTrue((simpleTypedProperty.getPropertyType().equals("test type")));
            assertTrue(simpleTypedProperty.getPropertyValue().equals("test value"));
        }
    }

    @Test
    public void testGetByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(){
        List<SimpleAnnotation> simpleAnnotations = annotationRepositoryService.getByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession("GSMTest2", "Accession2");

        Collection<BiologicalEntity> biologicalEntities = simpleAnnotations.get(0).getAnnotatedBiologicalEntities();
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
        AnnotationSource source = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/test"), "test", "", "");
        List<SimpleAnnotation> annotations = annotationRepositoryService.getByProvenanceSource(source, new PageRequest(0, 20));

        assertTrue(annotations.size() > 0);
    }

    @Test
    public void testGetProvenanceByProvenanceSource(){
        AnnotationSource source = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/test"), "test", "", "");
        List<AnnotationProvenance> annotationProvenances = annotationRepositoryService.getProvenanceByProvenanceSource(source, new PageRequest(0, 20));

        assertTrue(annotationProvenances.size() > 0);
    }

    @Test
    public void testGetByProvenanceSourceName(){
        List<SimpleAnnotation> annotations = annotationRepositoryService.getByProvenanceSourceName("test", new PageRequest(0, 20));
        assertTrue(annotations.size() > 0);
        assertTrue(annotations.get(0).getProvenance().getSource().getName().equals("test"));
    }

    @Test
    public void testGetAllBiologicalEntities(){
        Collection<BiologicalEntity> biologicalEntities = annotationRepositoryService.getAllBiologicalEntities();
        assertTrue(biologicalEntities.size() > 0);
    }

    @Test
    public void testGetAllStudies(){
        Collection<Study> studies = annotationRepositoryService.getAllStudies();
        assertTrue(studies.size() > 0);
    }

    @Test
    public void testGetAllDocuments() throws Exception {
        List<SimpleAnnotation> annotationDocumentList = annotationRepositoryService.getAllDocuments();
        assertThat("Not empty list", annotationDocumentList.size(), is(not(0)));
    }
//
//    @Test
//    public void testUpdate() throws Exception {
//        SimpleAnnotation annotationDocument = annotationRepositoryService.get("TestStringId");
//        annotationDocument.setAnnotatedProperty(new SimpleTypedProperty("TestProperty", "New Parameter", "New Value"));
//        annotationRepositoryService.update(annotationDocument);
//
//        annotationDocument = annotationRepositoryService.get("TestStringId");
//
//        assertThat("Value is New Value", annotationDocument.getAnnotatedProperty().getPropertyValue(), is("New Value"));
//    }

    @Test
    public void testGetByAnnotatedProperty() throws Exception {
        Property property = new SimpleTypedProperty("TestProperty", "test type", "test value");
        List<SimpleAnnotation> annotationDocument = annotationRepositoryService.getByAnnotatedPropertyValue("test value");

        assertThat("The Id should be TestStringId", annotationDocument.get(0).getId(), is("TestStringId"));

        Property aProperty = new SimpleTypedProperty("TestProperty","test type", "test value");
        annotationDocument = annotationRepositoryService.getByAnnotatedProperty(aProperty);

        assertThat("The Id should be TestStringId", annotationDocument.get(0).getId(), is("TestStringId"));
    }

}