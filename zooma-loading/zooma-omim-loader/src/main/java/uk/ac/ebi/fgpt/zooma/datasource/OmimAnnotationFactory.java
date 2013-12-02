package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimAnnotationFactory extends AbstractAnnotationFactory {
    public OmimAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return null;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return null;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator,
                                                                     AnnotationProvenance.Accuracy accuracy,
                                                                     Date annotationDate) {
        return null;
    }
}
