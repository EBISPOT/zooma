package uk.ac.ebi.spot.services;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.model.*;

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
public class AnnotationRepositoryServiceIT {

    @Autowired
    AnnotationRepositoryService annotationRepositoryService;

    @Autowired
    AnnotationSourceRepositoryService annotationSourceRepositoryService;

    @Autowired
    AnnotationProvenanceRepositoryService annotationProvenanceRepositoryService;

    @Autowired
    BiologicalEntityRepositoryService biologicalEntityRepositoryService;

    @Autowired
    StudyRepositoryService studyRepositoryService;

    @Before
    public void setup(){
        //Create an Annotation and store it in mongodb

        SimpleStudy simpleStudy = new SimpleStudy("Accession1", null);
        simpleStudy.setId("SS1");

        Collection<Study> studies = new ArrayList<>();
        studies.add(simpleStudy);

        simpleStudy = new SimpleStudy("Accession2", null);
        simpleStudy.setId("SS2");
        studies.add(simpleStudy);

        Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
        SimpleBiologicalEntity biologicalEntity = new SimpleBiologicalEntity("GSM374548", null, studies);
        biologicalEntity.setId("BE1");
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new SimpleBiologicalEntity("newEntity", null, studies);
        biologicalEntity.setId("BE2");
        biologicalEntities.add(biologicalEntity);

        Property property = new SimpleTypedProperty("disease", "lung cancer");
        URI semanticTag = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_0001071");
        Collection<URI> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        SimpleOntologyAnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/gxa"), "atlas","","");
        annotationSource.setId("TestSource");

        SimpleAnnotationProvenance annotationProvenance = new SimpleAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/gxa", new Date(), "Laura Huerta", new Date());
        annotationProvenance.setId("TestProvenance");

        SimpleAnnotation annotationDocument = new SimpleAnnotation(biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null,
                URI.create("URI"));

        annotationDocument.setId("TestStringId");
        annotationRepositoryService.save(annotationDocument);
    }

    @After
    public void teardown(){

        //remove source from the database
        SimpleAnnotationSource annotationSource = annotationSourceRepositoryService.get("TestSource");
        annotationSourceRepositoryService.delete(annotationSource);

        //remove provenance from the database
        SimpleAnnotationProvenance annotationProvenance = annotationProvenanceRepositoryService.get("TestProvenance");
        annotationProvenanceRepositoryService.delete(annotationProvenance);

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
        annotationDocument.setAnnotatedProperty(new SimpleTypedProperty("New Parameter", "New Value"));
        annotationRepositoryService.update(annotationDocument);

        annotationDocument = annotationRepositoryService.get("TestStringId");

        assertThat("Value is New Value", annotationDocument.getAnnotatedProperty().getPropertyValue(), is("New Value"));
    }

    @Test
    public void testGetByAnnotatedProperty() throws Exception {
        Property property = new SimpleTypedProperty("disease", "lung cancer");
        SimpleAnnotation annotationDocument = annotationRepositoryService.getByAnnotatedProperty(property);

        assertThat("The Id is TestStringId", annotationDocument.getId(), is("TestStringId"));
    }

    @Test
    public void testGetByURI() throws Exception {
        URI uri = URI.create("URI");
        SimpleAnnotation annotationDocument = annotationRepositoryService.getByUri(uri);

        assertThat("The Id is TestStringId", annotationDocument.getId(), is("TestStringId"));

    }

}