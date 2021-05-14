package uk.ac.ebi.fgpt.zooma.util;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Simon Jupp
 * @date 30/10/2013 Functional Genomics Group EMBL-EBI
 */
public class TestAnnotationEvidence {

    @Test
    public void testEvidenceCode() {
        AnnotationProvenance.Evidence e = AnnotationProvenance.Evidence.UNKNOWN;
        assertTrue(e.name().equals("UNKNOWN"));
    }
}
