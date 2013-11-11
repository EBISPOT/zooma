package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;

import java.net.URI;

/**
 * A loading session for loading ontologies from files.  This primarily acts as a wrapper around an {@link uk.ac.ebi.fgpt.zooma.owl.OntologyLoader} to
 * acquire the ontology namespace, which is used to mint URIs in the annotation model
 *
 * @author Tony Burdett
 * @author James Malone
 * @date 23/10/12
 */
public class OWLLoadingSession extends AbstractAnnotationLoadingSession {
    private final String baseNamespace;

    public OWLLoadingSession(OntologyLoader owlLoader) {
//        this.baseNamespace = URIUtils.normalizeURI(owlLoader.getOntologyURI()).toString() + "/";
        this.baseNamespace = Namespaces.OWL_RESOURCE.getURI().toString();
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        throw new UnsupportedOperationException("Cannot mint study URIs for OWLAnnotation datasources - " +
                                                        "ontology mapping does not allow for creation of studies.");
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        throw new UnsupportedOperationException("Cannot mint bioentity URIs for OWLAnnotation datasources - " +
                                                        "ontology mapping does not allow for creation of bioentities.");
    }


    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(baseNamespace  + annotationID);
    }
}
