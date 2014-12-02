package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * Created by dwelter on 28/05/14.
 */
public class ChemblLoadingSession extends AbstractAnnotationLoadingSession {
    protected ChemblLoadingSession() {
        super(new SimpleAnnotationProvenanceTemplate(new SimpleDatabaseAnnotationSource(Namespaces.CHEMBL.getURI(),
                                                                                        "chembl"),
                                                     AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                     AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                     "ZOOMA",
                                                     new Date(),
                                                     null,
                                                     null),
              URI.create("http://purl.obolibrary.org/obo/CLO_0000031"),
              null);
    }
}
