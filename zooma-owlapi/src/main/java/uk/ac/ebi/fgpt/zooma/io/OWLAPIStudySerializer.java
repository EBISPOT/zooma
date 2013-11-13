package uk.ac.ebi.fgpt.zooma.io;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;

/**
 * A ZOOMA serializer that uses the OWL API to generate RDF from studies
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 07/08/13
 */
public class OWLAPIStudySerializer extends OWLAPIZoomaSerializer<Study> {
    private final IRI studyClassIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "DatabaseEntrySource");

    public OWLAPIStudySerializer() {
        super();
    }

    public OWLAPIStudySerializer(OWLOntologyFormat outputFormat) {
        super(outputFormat);
    }

    @Override public OWLIndividual serialize(Study study, OWLOntology ontology)
            throws ZoomaSerializationException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass studyClass = factory.getOWLClass(studyClassIRI);

        getLog().trace("Creating IRI for study owl individual: " + study.getURI());
        IRI studyIRI = createIRI(study.getURI());
        getLog().trace("Creating study owl individual");
        OWLNamedIndividual studyInstance = factory.getOWLNamedIndividual(studyIRI);
        getLog().trace("Created study owl individual type");

        if (!study.getTypes().isEmpty()) {
            for (URI uri : study.getTypes()) {
                OWLClass cls = factory.getOWLClass(IRI.create(uri));
                manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(cls, studyInstance));
            }
        }

        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(studyClass, studyInstance));


        // create a label
        if (study.getAccession() != null) {
            OWLAnnotationProperty labelProperty =
                    factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
            manager.addAxiom(ontology,
                             factory.getOWLAnnotationAssertionAxiom(labelProperty,
                                                                    studyInstance.getIRI(),
                                                                    factory.getOWLLiteral(study.getAccession())));
        }

        return studyInstance;
    }
}
