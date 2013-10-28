package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An ArrayExpress sample annotation factory, used for generating ArrayExpress sample annotations
 *
 * @author Tony Burdett
 * @date 01/10/12
 */
public class ArrayExpressAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public ArrayExpressAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.ARRAYEXPRESS.getURI(), "arrayexpress"),
                                                         AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                                                         "ZOOMA",
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.ARRAYEXPRESS.getURI(), "arrayexpress"),
                                              AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
