package uk.ac.ebi.spot;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.config.MongoConfig;
import uk.ac.ebi.spot.services.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
        SimpleBiologicalEntity biologicalEntity = new SimpleBiologicalEntity("BE1", "GSM374548", studies);
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new SimpleBiologicalEntity("BE2", "newEntity", studies);
        biologicalEntities.add(biologicalEntity);

        Property property = new SimpleTypedProperty("TestProperty", "disease", "lung cancer");
        URI semanticTag = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_0001071");
        Collection<URI> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        SimpleOntologyAnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/gxa"), "atlas","","");

        SimpleAnnotationProvenance annotationProvenance = new SimpleAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/gxa", new Date(), "Laura Huerta", new Date());

        SimpleAnnotation annotationDocument = new SimpleAnnotation("TestStringId", biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);

        annotationRepositoryService.save(annotationDocument);
    }

    @After
    public void teardown(){

        //remove property
        propertyService.delete(propertyService.get("TestProperty"));

        //remove studies
        studyRepositoryService.delete(studyRepositoryService.get("SS1"));
        studyRepositoryService.delete(studyRepositoryService.get("SS2"));

        //remove biological entities
        biologicalEntityRepositoryService.delete(biologicalEntityRepositoryService.get("BE1"));
        biologicalEntityRepositoryService.delete(biologicalEntityRepositoryService.get("BE2"));

        //remove the annotation from the database
        SimpleAnnotation annotationDocument = annotationRepositoryService.get("TestStringId");

        annotationRepositoryService.delete(annotationDocument);

        assertNull(annotationRepositoryService.get(annotationDocument.getId()));
    }

    @Test
    public void testGetAllDocuments() throws Exception {
        List<SimpleAnnotation> annotationDocumentList = annotationRepositoryService.getAllDocuments();
        Collection<URI> sem = annotationDocumentList.get(0).getSemanticTags();
        assertThat("Not empty list", annotationDocumentList.size(), is(not(0)));
    }

    @Test
    public void testUpdate() throws Exception {
        SimpleAnnotation annotationDocument = annotationRepositoryService.get("TestStringId");
        annotationDocument.setAnnotatedProperty(new SimpleTypedProperty("TestProperty", "New Parameter", "New Value"));
        annotationRepositoryService.update(annotationDocument);

        annotationDocument = annotationRepositoryService.get("TestStringId");

        assertThat("Value is New Value", annotationDocument.getAnnotatedProperty().getPropertyValue(), is("New Value"));
    }

    @Test
    public void testGetByAnnotatedProperty() throws Exception {
        Property property = new SimpleTypedProperty("TestProperty", "disease", "lung cancer");
        SimpleAnnotation annotationDocument = annotationRepositoryService.getByAnnotatedProperty(property);

        assertThat("The Id is TestStringId", annotationDocument.getId(), is("TestStringId"));
    }

}