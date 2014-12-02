package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An annotation loading session that is capable of minting URIs specific to ArrayExpress
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public abstract class ArrayExpressLoadingSession extends AbstractAnnotationLoadingSession {
    protected ArrayExpressLoadingSession(String datasourceName, URI defaultBiologicalEntityUri, URI defaultStudyEntityUri) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(Namespaces.ARRAYEXPRESS.getURI(),
                                                         datasourceName),
                      AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                      "ZOOMA",
                      new Date()),
              defaultBiologicalEntityUri,
              defaultStudyEntityUri);
    }
}
