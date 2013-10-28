package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An OMIA annotation factory, used for generating annotation objects that have been obtained from the OMIA database.
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public OmiaAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.OMIA.getURI(), "omia"),
                                                         AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                         "ZOOMA",
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.OMIA.getURI(), "omia"),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
