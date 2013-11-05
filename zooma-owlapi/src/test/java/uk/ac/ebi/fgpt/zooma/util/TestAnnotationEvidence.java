package uk.ac.ebi.fgpt.zooma.util;

import junit.framework.TestCase;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;

/**
 * @author Simon Jupp
 * @date 30/10/2013
 * Functional Genomics Group EMBL-EBI
 */
public class TestAnnotationEvidence extends TestCase {

    public void testEvidenceCode() {
        AnnotationProvenance.Evidence e = AnnotationProvenance.Evidence.UNKNOWN;
        assertTrue(e.name().equals("UNKNOWN"));
    }
}
