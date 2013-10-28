package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.fgpt.zooma.owl.OntologyLoader;

import java.net.URI;
import java.util.Date;

/**
 * An annotation factory that is capable of generating annotations that have been derived from an ontology
 *
 * @author Tony Burdett
 * @author James Malone
 * @date 23/10/12
 */
public class OWLAnnotationFactory extends AbstractAnnotationFactory {
    private final URI namespace;
    private final String name;
    private final AnnotationProvenance provenance;

    public OWLAnnotationFactory(AnnotationLoadingSession annotationLoadingSession, OntologyLoader owlLoader) {
        super(annotationLoadingSession);
        this.namespace = owlLoader.getOntologyIRI().toURI();
        this.name = owlLoader.getOntologyName();
        this.provenance = new SimpleAnnotationProvenance(new SimpleOntologyAnnotationSource(namespace, name),
                                                         AnnotationProvenance.Evidence.COMPUTED_FROM_ONTOLOGY,
                                                         namespace.toString(),
                                                         new Date());
    }

    @Override protected AnnotationProvenance getAnnotationProvenance() {
        return provenance;
    }

    @Override protected AnnotationProvenance getAnnotationProvenance(String annotator, Date annotationDate) {
        return new SimpleAnnotationProvenance(new SimpleOntologyAnnotationSource(namespace, name),
                                              AnnotationProvenance.Evidence.COMPUTED_FROM_ONTOLOGY,
                                              namespace.toString(),
                                              new Date(),
                                              annotator,
                                              annotationDate);
    }
}
