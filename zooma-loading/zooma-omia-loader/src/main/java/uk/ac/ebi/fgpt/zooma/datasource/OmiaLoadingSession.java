package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An OMIA annotation loading session that can generate URIs specific to annotations from OMIA.
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaLoadingSession extends AbstractAnnotationLoadingSession {
    public OmiaLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.OMIA.getURI(),
                                                                                        "omia"),
                                                     AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null));
    }
}
