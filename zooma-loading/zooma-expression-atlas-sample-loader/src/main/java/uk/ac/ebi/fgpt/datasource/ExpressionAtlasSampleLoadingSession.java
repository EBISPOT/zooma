package uk.ac.ebi.fgpt.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.datasource.AbstractAnnotationLoadingSession;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas samples
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public class ExpressionAtlasSampleLoadingSession extends AbstractAnnotationLoadingSession {
    protected ExpressionAtlasSampleLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.ATLAS.getURI(),
                                                                                        "atlas"),
                                                     AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null),
              URI.create("http://purl.obolibrary.org/obo/OBI_0000747"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }
}
