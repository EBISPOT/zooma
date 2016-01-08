package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;
import uk.ac.ebi.fgpt.zooma.util.AnnotationProvenanceBuilder;

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
    private String datasourceName;

    public OWLLoadingSession(OntologyLoader owlLoader) {
        super();
        setAnnotationProvenanceTemplate(
                AnnotationProvenanceBuilder
                        .createTemplate(owlLoader.getOntologyIRI().toURI().toString())
                        .sourceIs(new SimpleOntologyAnnotationSource(owlLoader.getOntologyIRI().toURI(),
                                                                     owlLoader.getOntologyName()))
                        .evidenceIs(AnnotationProvenance.Evidence.COMPUTED_FROM_ONTOLOGY));
        this.datasourceName = owlLoader.getOntologyName();

    }
}
