package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import uk.ac.ebi.fgpt.zooma.datasource.OntologyDAO;

import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 18/02/13
 */
public class TestLabelUtils {
    private URI entity1;
    private URI entity2;
    private URI entityEmpty;


    private String testLabel1;
    private String testLabel2;

    private String emptyLabel;

    private String testSynonym;

    private OntologyDAO ontologyDAO;

    @BeforeEach
    public void setup() {
        testLabel1 = "label one";
        testLabel2 = "label two";

        emptyLabel = "";

        testSynonym = "synonym one";

        entity1 = URI.create("http://www.test.com/labels/1");
        entity2 = URI.create("http://www.test.com/labels#2");
        entityEmpty = URI.create("http://www.test.com/labels/empty");

        ontologyDAO = mock(OntologyDAO.class);
        when(ontologyDAO.getSemanticTagLabel(entity1)).thenReturn(testLabel1);
        when(ontologyDAO.getSemanticTagLabel(entity2)).thenReturn(testLabel2);
        when(ontologyDAO.getSemanticTagLabel(entityEmpty)).thenReturn(emptyLabel);
        when(ontologyDAO.getSemanticTagSynonyms(entity1)).thenReturn(Collections.singleton(testSynonym));
    }

    @AfterEach
    public void teardown() {
        ontologyDAO = null;
        LabelUtils utils = new LabelUtils();
        utils.setOntologyDAO(null);
    }

    @Test
    public void testGetLabel() {
        // set ontology DAO
        LabelUtils utils = new LabelUtils();
        utils.setOntologyDAO(ontologyDAO);

        // now create a new LabelUtils, check ontology dao is still set
        LabelUtils utils2 = new LabelUtils();
        assertEquals(ontologyDAO, utils2.getOntologyDAO());

        // now run tests
        assertEquals(testLabel1, LabelUtils.getPrimaryLabel(entity1));
        assertEquals(testLabel2, LabelUtils.getPrimaryLabel(entity2));
        assertNotNull(LabelUtils.getPrimaryLabel(entityEmpty));
    }

    @Test
    public void testUninitialized() {
        // test without setting an ontology dao
        try {
            String label = LabelUtils.getPrimaryLabel(entity1);
            fail("Checking label with null ontology DAO should throw an error - actually, just returned " + label);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
