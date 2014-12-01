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
    private final String datasource;
    private final String generator;
    private final String datasourceName;
    private final AnnotationProvenance provenance;

    public CSVAnnotationFactory(CSVLoadingSession annotationLoadingSession) {
        this("zooma", annotationLoadingSession);
    }

    public CSVAnnotationFactory(String datasourceURL,
                                String datasourceName,
                                CSVLoadingSession annotationLoadingSession) {
        this(datasourceURL, datasourceName, "zooma", annotationLoadingSession);
    }

    public CSVAnnotationFactory(String annotationCreator, CSVLoadingSession annotationLoadingSession) {
        this(null, null, annotationCreator, annotationLoadingSession);
    }

    public CSVAnnotationFactory(String datasourceURL,
                                String datasourceName,
                                String annotationCreator,
                                CSVLoadingSession annotationLoadingSession) {
        super(annotationLoadingSession);
        this.datasource = datasourceURL == null ? annotationLoadingSession.getNamespace().toString() : datasourceURL;

        this.datasourceName = datasourceName;
        this.generator = annotationCreator;
        this.provenance =
                new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(URI.create(datasource),
                                                                                  datasourceName),
                                               AnnotationProvenance.Evidence.MANUAL_CURATED,
                                               generator,
                                               new Date());
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    @Override
    protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override
    protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(URI.create(datasource),
                                                                                 datasourceName),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                              generator,
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }

    @Override
    protected AnnotationProvenance getAnnotationProvenance(String annotator,
                                                           AnnotationProvenance.Accuracy accuracy,
                                                           Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(URI.create(datasource),
                                                                                 datasourceName),
                                              AnnotationProvenance.Evidence.MANUAL_CURATED,
                                              accuracy,
                                              generator,
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
