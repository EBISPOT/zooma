package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimLoadingSession extends AbstractAnnotationLoadingSession {
    protected OmimLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.OMIM.getURI(),
                                                                                        "omim"),
                                                     AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null));
    }
}
