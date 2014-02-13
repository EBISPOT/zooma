package uk.ac.ebi.fgpt.zooma;

import uk.ac.ebi.fgpt.zooma.datasource.AbstractAnnotationFactory;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationLoadingSession;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An expression atlas annotation factory, used for generating Expression Atlas annotations
 *
 * @author Tony Burdett
 * @date 13/02/14
 */
public class ExpressionAtlasAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public ExpressionAtlasAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance =
                new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.ATLAS.getURI(),
                                                                                  "atlas"),
                                               AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                                               "ZOOMA",
                                               new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override
    protected AnnotationProvenance getAnnotationProvenance(String annotator,
                                                           AnnotationProvenance.Accuracy accuracy,
                                                           Date annotationDate) {
        return null; // todo
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.ATLAS.getURI(),
                                                                                 "atlas"),
                                              AnnotationProvenance.Evidence.SUBMITTER_PROVIDED,
                                              AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
