package uk.ac.ebi.fgpt.zooma.datasource;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import uk.ac.ebi.fgpt.zooma.owl.AssertedOntologyLoader;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;
import uk.ac.ebi.fgpt.zooma.owl.ReasonedOntologyLoader;

import java.net.URI;
import java.util.Collection;

/**
 * Factory class for OWL-based ZOOMA datasources.  This class can be used to generate fully formed, pre-configured
 * {@link uk.ac.ebi.fgpt.zooma.datasource.OWLAnnotationDAO} objects that are immediately ready to use.
 *
 * @author Tony Burdett
 * @date 25/06/13
 */
public class OWLDatasourceFactory {
    public static AnnotationDAO generateDatasource(String datasourceName, URI datasourceURI) {
        return generateDatasource(datasourceName, datasourceURI, null, null, true);
    }

    public static AnnotationDAO generateDatasource(String datasourceName, URI datasourceURI, boolean useReasoning) {
        return generateDatasource(datasourceName, datasourceURI, null, null, useReasoning);
    }

    public static AnnotationDAO generateDatasource(String datasourceName, URI datasourceURI, String loadFrom) {
        return generateDatasource(datasourceName, datasourceURI, loadFrom, null, true);
    }

    public static AnnotationDAO generateDatasource(String datasourceName,
                                                   URI datasourceURI,
                                                   String loadFrom,
                                                   boolean useReasoning) {
        return generateDatasource(datasourceName, datasourceURI, loadFrom, null, useReasoning);
    }

    public static AnnotationDAO generateDatasource(String datasourceName,
                                                   URI ontologyURI,
                                                   String loadFrom,
                                                   Collection<URI> synonymURIs,
                                                   boolean useReasoning) {
        // loadFrom is optional, may be null
        Resource ontologyResource = null;
        if (StringUtils.hasText(loadFrom)) {
            ontologyResource = new DefaultResourceLoader().getResource(loadFrom);
        }

        // generate and wire up classes...
        OntologyLoader owlLoader;
        if (useReasoning) {
            ReasonedOntologyLoader rOwlLoader = new ReasonedOntologyLoader();
            rOwlLoader.setOntologyURI(ontologyURI);
            if (ontologyResource != null) {
                rOwlLoader.setOntologyResource(ontologyResource);
            }
            if (synonymURIs != null) {
                rOwlLoader.getSynonymURIs().addAll(synonymURIs);
            }
            rOwlLoader.init();
            owlLoader = rOwlLoader;
        }
        else {
            AssertedOntologyLoader aOwlLoader = new AssertedOntologyLoader();
            aOwlLoader.setOntologyURI(ontologyURI);
            if (ontologyResource != null) {
                aOwlLoader.setOntologyResource(ontologyResource);
            }
            if (synonymURIs != null) {
                aOwlLoader.getSynonymURIs().addAll(synonymURIs);
            }
            aOwlLoader.init();
            owlLoader = aOwlLoader;
        }

        AnnotationLoadingSession owlLoadingSession = new OWLLoadingSession(owlLoader);

        AnnotationFactory owlAnnotationFactory = new OWLAnnotationFactory(owlLoadingSession, owlLoader);

        OWLAnnotationDAO owlAnnotationDAO = new OWLAnnotationDAO(owlAnnotationFactory, owlLoader, datasourceName);
        owlAnnotationDAO.init();

        return owlAnnotationDAO;
    }
}
