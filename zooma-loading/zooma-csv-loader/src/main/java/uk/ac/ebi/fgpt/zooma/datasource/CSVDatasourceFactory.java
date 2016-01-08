package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 28/05/15
 */
public class CSVDatasourceFactory {
    public static AnnotationDAO generateDatasource(String datasourceName, URI datasourceURI) {
        return generateDatasource(datasourceName, datasourceURI, null, null, null);
    }

    public static AnnotationDAO generateDatasource(String datasourceName, URI datasourceURI, String loadFrom) {
        return generateDatasource(datasourceName, datasourceURI, loadFrom, null, null);
    }

    public static AnnotationDAO generateDatasource(String datasourceName,
                                                   URI datasourceURI,
                                                   String loadFrom,
                                                   String delimiter) {
        return generateDatasource(datasourceName, datasourceURI, loadFrom, null, delimiter);
    }

    public static AnnotationDAO generateDatasource(String datasourceName,
                                                   URI datasourceURI,
                                                   String loadFrom,
                                                   String annotationCreator,
                                                   String delimiter) {
        // loadFrom is optional, may be null
        Resource csvResource = null;
        if (StringUtils.hasText(loadFrom)) {
            csvResource = new DefaultResourceLoader().getResource(loadFrom);
        }
        else {
            csvResource = new DefaultResourceLoader().getResource(datasourceURI.toString());
        }

        // generate and wire up classes...
        AnnotationLoadingSession csvLoadingSession;
        if (annotationCreator != null) {
            csvLoadingSession = new CSVLoadingSession(datasourceURI, datasourceName, annotationCreator);
        }
        else {
            csvLoadingSession = new CSVLoadingSession(datasourceURI, datasourceName);
        }

        AnnotationFactory csvAnnotationFactory = new DefaultAnnotationFactory(csvLoadingSession);

        CSVAnnotationDAO csvAnnotationDAO;
        if (delimiter == null) {
            csvAnnotationDAO = new CSVAnnotationDAO(csvAnnotationFactory, csvResource);
        }
        else {
            csvAnnotationDAO = new CSVAnnotationDAO(csvAnnotationFactory, csvResource, delimiter);
        }
        csvAnnotationDAO.init();

        return csvAnnotationDAO;
    }

}
