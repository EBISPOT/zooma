package uk.ac.ebi.fgpt.zooma.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestDAOBasedAnnotationService {
    private AnnotationDAO dao;
    private DAOBasedAnnotationService service;

    private Study study;
    private BiologicalEntity biologicalEntity;
    private Property property;
    private URI semanticTagURI;
    private URI uri;


    @Before
    public void setUp() {
        dao = mock(AnnotationDAO.class);
        service = new DAOBasedAnnotationService();
        service.setAnnotationDAO(dao);

        study = mock(Study.class);
        biologicalEntity = mock(BiologicalEntity.class);
        property = mock(Property.class);
        semanticTagURI = URI.create("http://test.org/entity");
        uri = URI.create("http://test.org/uri");
    }

    @After
    public void tearDown() {
        service = null;
        dao = null;
    }

    @Test
    public void testGetAnnotations() {
        service.getAnnotations();
        verify(dao).read();
    }

    @Test
    public void testGetAnnotationsLimited() {
        int limit = 10;
        int start = 5;
        service.getAnnotations(limit, start);
        verify(dao).read(limit, start);
    }

    @Test
    public void testGetAnnotationsByStudy() {
        service.getAnnotationsByStudy(study);
        verify(dao).readByStudy(study);
    }

    @Test
    public void testGetAnnotationsByBiologicalEntity() {
        service.getAnnotationsByBiologicalEntity(biologicalEntity);
        verify(dao).readByBiologicalEntity(biologicalEntity);
    }

    @Test
    public void testGetAnnotationsByProperty() {
        service.getAnnotationsByProperty(property);
        verify(dao).readByProperty(property);
    }

    @Test
    public void testGetAnnotationsBySemanticTag() {
        service.getAnnotationsBySemanticTag(semanticTagURI);
        verify(dao).readBySemanticTag(semanticTagURI);
    }

    @Test
    public void getAnnotation() {
        service.getAnnotation(uri);
        verify(dao).read(uri);
    }
}
