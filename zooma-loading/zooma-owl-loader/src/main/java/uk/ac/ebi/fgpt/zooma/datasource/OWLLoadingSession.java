package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;

import java.util.Date;

/**
 * A loading session for loading ontologies from files.  This primarily acts as a wrapper around an {@link
 * uk.ac.ebi.fgpt.zooma.owl.OntologyLoader} to acquire the ontology namespace, which is used to mint URIs in the
 * annotation model
 *
 * @author Tony Burdett
 * @author James Malone
 * @date 23/10/12
 */
public class OWLLoadingSession extends AbstractAnnotationLoadingSession {
    public OWLLoadingSession(OntologyLoader owlLoader) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleOntologyAnnotationSource(owlLoader.getOntologyIRI().toURI(),
                                                         owlLoader.getOntologyName()),
                      AnnotationProvenance.Evidence.COMPUTED_FROM_ONTOLOGY,
                      owlLoader.getOntologyIRI().toURI().toString(),
                      new Date()),
              null,
              null);
    }
}
