package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


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
            assertEquals(testIri1, iri, "IRI doesn't match that expected from lookup");

            iri = OntologyAccessionUtils.getIRIFromAccession(testAccession2);
            assertEquals(testIri2, iri, "IRI doesn't match that expected from lookup");

            // do a reverse lookup
            String accession;
            accession = OntologyAccessionUtils.getAccessionFromIRI(testIri1);
            assertEquals(testAccession1, accession, "Accession doesn't match that expected from lookup");

            accession = OntologyAccessionUtils.getAccessionFromIRI(testIri2);
            assertEquals(testAccession2, accession,"Accession doesn't match that expected from lookup");
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
