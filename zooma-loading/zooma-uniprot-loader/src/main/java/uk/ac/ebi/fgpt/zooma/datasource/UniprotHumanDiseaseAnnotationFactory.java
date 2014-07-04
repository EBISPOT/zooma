package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.util.Date;

/**
 * An annotation factory that is tailored to produce the correct provenance for the Uniprot database
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseAnnotationFactory extends AbstractAnnotationFactory {
    private final AnnotationProvenance provenance;

    public UniprotHumanDiseaseAnnotationFactory(AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.provenance =
                new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.UNIPROT.getURI(),
                                                                                  "uniprot"),
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
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.UNIPROT.getURI(),
                                                                                 "uniprot"),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              accuracy,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);

    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(Namespaces.UNIPROT.getURI(),
                                                                                 "uniprot"),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                              "ZOOMA",
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
