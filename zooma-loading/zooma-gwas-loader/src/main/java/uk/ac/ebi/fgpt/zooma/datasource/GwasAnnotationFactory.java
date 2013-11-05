package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An GWAS sample annotation factory, used for generating GWAS annotations
 *
 * @author Dani Welter
 * @date 06/11/12
 */
public class GwasAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public GwasAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.GWAS.getURI(), "gwas"),
			                                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                         "ZOOMA",
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override
    protected AnnotationProvenance getAnnotationProvenance(String annotator, AnnotationProvenance.Accuracy accuracy, Date annotationDate) {
        return null;    // todo
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.GWAS.getURI(), "gwas"),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
