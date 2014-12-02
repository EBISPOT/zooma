package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An annotation loading session that mints URIs for annotation objects acquired from the Uniprot database
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseAnnotationSession extends AbstractAnnotationLoadingSession {
    public UniprotHumanDiseaseAnnotationSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.UNIPROT.getURI(),
                                                                                        "uniprot"),
                                                     AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null));
    }
}
