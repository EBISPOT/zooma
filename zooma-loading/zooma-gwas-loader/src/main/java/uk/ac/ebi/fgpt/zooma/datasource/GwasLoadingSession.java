package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An annotation loading session that is capable of minting URIs specific to GWAS data
 *
 * @author Dani Welter
 * @date 06/11/12
 */
public class GwasLoadingSession extends AbstractAnnotationLoadingSession {
    protected GwasLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(Namespaces.GWAS.getURI(), "gwas"),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                      "ZOOMA",
                      new Date(),
                      null,
                      null),
              URI.create("http://purl.obolibrary.org/obo/SO_0000694"),
              null);
    }
}
