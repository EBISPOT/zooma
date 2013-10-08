package uk.ac.ebi.fgpt.zooma.util;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * @author Tony Burdett
 * @date 23/10/12
 */
public class TestOntologyAccessionUtils {
    private final IRI testIri1 = IRI.create("http://www.ebi.ac.uk/zooma/test#0001");
    private final IRI testIri2 = IRI.create("http://www.ebi.ac.uk/zooma/test/test_0002");
    private final String testAccession1 = "0001";
    private final String testAccession2 = "test_0002";

    @Test
    public void testLoadAndGetIri() {
        URL testOntURL = getClass().getClassLoader().getResource("test.owl");

        try {
            // load this ontology
            OntologyAccessionUtils.loadOntology(testOntURL);

            // do forward lookups
            IRI iri;
            iri = OntologyAccessionUtils.getIRIFromAccession(testAccession1);
            assertEquals("IRI doesn't match that expected from lookup", testIri1, iri);

            iri = OntologyAccessionUtils.getIRIFromAccession(testAccession2);
            assertEquals("IRI doesn't match that expected from lookup", testIri2, iri);

            // do a reverse lookup
            String accession;
            accession = OntologyAccessionUtils.getAccessionFromIRI(testIri1);
            assertEquals("Accession doesn't match that expected from lookup", testAccession1, accession);

            accession = OntologyAccessionUtils.getAccessionFromIRI(testIri2);
            assertEquals("Accession doesn't match that expected from lookup", testAccession2, accession);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
