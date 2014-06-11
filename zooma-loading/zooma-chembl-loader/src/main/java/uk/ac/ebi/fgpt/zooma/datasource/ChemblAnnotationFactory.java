package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * Created by dwelter on 28/05/14.
 */
public class ChemblAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public ChemblAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance =
                new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.CHEMBL.getURI(), "chembl"),
                        AnnotationProvenance.Evidence.MANUAL_CURATED,
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
        return null;    // todo
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.CHEMBL.getURI(), "chembl"),
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "ZOOMA",
                new Date(),
                annotator,
                annotationDate);
    }
}
