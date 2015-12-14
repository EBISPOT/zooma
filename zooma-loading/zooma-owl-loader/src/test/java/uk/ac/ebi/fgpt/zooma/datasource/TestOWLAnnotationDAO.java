package uk.ac.ebi.fgpt.zooma.datasource;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 23/05/14
 */
public class TestOWLAnnotationDAO {
    private OWLOntology ontology;
    private OntologyLoader owlLoader;

    private AnnotationFactory annotationFactory;
    private OWLAnnotationDAO annotationDAO;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Before
    public void setUp() {
        try {
            IRI testOntologyIRI = IRI.create(getClass().getClassLoader().getResource("test.owl"));
            ontology = OWLManager.createOWLOntologyManager().loadOntology(testOntologyIRI);

            owlLoader = mock(OntologyLoader.class);
            when(owlLoader.getOntology()).thenReturn(ontology);

            annotationFactory = mock(AnnotationFactory.class);
            annotationDAO = new OWLAnnotationDAO(annotationFactory, owlLoader, "test");
        }
//Before :        catch (URISyntaxException | OWLOntologyCreationException e) {
//now :
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetSupplementaryRDFStream() {
        // read bytes from input stream, write to file
        try (InputStream in = annotationDAO.getSupplementaryRDFStream();
             OutputStream out = new FileOutputStream(new File("target", "test_supplemental.rdf"))) {
            getLog().debug("Reading from supplementary RDF data stream...");
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            getLog().debug("Read next " + len + " bytes");
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
                getLog().debug("Read next " + len + " bytes");
            }
            getLog().debug("Finished reading from RDF stream");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
