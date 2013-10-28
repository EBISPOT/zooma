package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * A comma-separated text file annotation factory that can generate annotations from a text file.
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVAnnotationFactory extends AbstractAnnotationFactory {
    private final String namespace;
    private final String generator;
    private final String name;
    private final AnnotationProvenance provenance;

    public CSVAnnotationFactory(String namespace,
                                String name,
                                String annotationCreator,
                                AnnotationLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.namespace = namespace;
        this.name = name;
        this.generator = annotationCreator;
        this.provenance = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(URI.create(namespace), name),
                                                         AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                         generator,
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(URI.create(namespace), name),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              generator,
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
