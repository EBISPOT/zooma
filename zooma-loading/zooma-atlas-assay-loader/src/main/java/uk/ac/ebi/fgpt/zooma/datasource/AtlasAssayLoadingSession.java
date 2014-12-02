package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An atlas annotation loading session that can generate URIs specific to atlas assays
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public class AtlasAssayLoadingSession extends AbstractAnnotationLoadingSession {
    protected AtlasAssayLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.GXA.getURI(), "gxa"),
                                                     AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null),
              URI.create("http://purl.obolibrary.org/obo/OBI_0000070"),
              URI.create("http://www.ebi.ac.uk/efo/EFO_0004033"));
    }
}
