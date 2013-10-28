package uk.ac.ebi.fgpt.zooma.io;

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;

/**
 * A ZOOMA serializer that uses the OWL API to generate RDF from biological entities
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 07/08/13
 */
public class OWLAPIBiologicalEntitySerializer extends OWLAPIZoomaSerializer<BiologicalEntity> {
    private final IRI dataItemIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "Target");

    private final IRI isPartOfPropertyIRI = IRI.create(Namespaces.DC.getURI() + "isPartOf");

    private final OWLAPIStudySerializer studySerializer;

    public OWLAPIBiologicalEntitySerializer() {
        this(new RDFXMLOntologyFormat());
    }

    public OWLAPIBiologicalEntitySerializer(OWLOntologyFormat outputFormat) {
        this(outputFormat, new OWLAPIStudySerializer(outputFormat));
    }

    public OWLAPIBiologicalEntitySerializer(OWLOntologyFormat outputFormat,
                                            OWLAPIStudySerializer studySerializer) {
        super(outputFormat);
        this.studySerializer = studySerializer;
    }

    public OWLAPIStudySerializer getStudySerializer() {
        return studySerializer;
    }

    @Override public OWLIndividual serialize(BiologicalEntity biologicalEntity, OWLOntology ontology)
            throws ZoomaSerializationException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass bioentityClass = factory.getOWLClass(dataItemIRI);
        getLog().trace("Creating IRI for bioentity owl individual: " + biologicalEntity.getURI());
        IRI beIRI = createIRI(biologicalEntity.getURI());
        getLog().trace("Creating bioentity owl individual");
        OWLNamedIndividual bioentityInstance = factory.getOWLNamedIndividual(beIRI);
        getLog().trace("Created bioentity owl individual");
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(bioentityClass, bioentityInstance));

        for (URI uri : biologicalEntity.getTypes()) {
            OWLClass beType = factory.getOWLClass(IRI.create(uri));
            manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(beType, bioentityInstance));
        }

        // create a label
        if (biologicalEntity.getName() != null) {
            OWLAnnotationProperty labelProperty =
                    factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
            manager.addAxiom(ontology,
                             factory.getOWLAnnotationAssertionAxiom(labelProperty,
                                                                    bioentityInstance.getIRI(),
                                                                    factory.getOWLLiteral(biologicalEntity.getName())));
        }

        for (Study study : biologicalEntity.getStudies()) {
            // convert studies on this bioentity
            OWLIndividual studyIndividual = getStudySerializer().serialize(study, ontology);

            // associate study and biological entity
            assertBiologicalEntityIsPartOf(bioentityInstance, studyIndividual, ontology);
        }

        return bioentityInstance;
    }

    private void assertBiologicalEntityIsPartOf(OWLIndividual biologicalEntityIndividual,
                                                OWLIndividual studyIndividual,
                                                OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty isPartOfProperty = factory.getOWLObjectProperty(isPartOfPropertyIRI);

        manager.addAxiom(ontology,
                         factory.getOWLObjectPropertyAssertionAxiom(isPartOfProperty,
                                                                    biologicalEntityIndividual,
                                                                    studyIndividual));
    }
}
