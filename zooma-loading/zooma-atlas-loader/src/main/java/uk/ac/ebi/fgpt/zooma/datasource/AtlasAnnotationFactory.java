package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An Atlas sample annotation factory, used for generating Atlas annotations
 *
 * @author Tony Burdett
 * @date 01/10/12
 */
public class AtlasAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public AtlasAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.GXA.getURI(), "gxa"),
                                                         AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                                                         "ZOOMA",
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.GXA.getURI(), "gxa"),
                                              AnnotationProvenance.Evidence.ZOOMA_INFERRED_FROM_CURATED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
