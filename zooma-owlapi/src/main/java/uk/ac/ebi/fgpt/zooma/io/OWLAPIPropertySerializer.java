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
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

/**
 * A ZOOMA serializer that uses the OWL API to generate RDF from properties
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 07/08/13
 */
public class OWLAPIPropertySerializer extends OWLAPIZoomaSerializer<Property> {
    private final IRI propertyClassIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "Property");

    private final IRI propertyNamePropertyIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "propertyName");
    private final IRI propertyValuePropertyIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "propertyValue");

    public OWLAPIPropertySerializer() {
        super();
    }

    public OWLAPIPropertySerializer(OWLOntologyFormat outputFormat) {
        super(outputFormat);
    }

    @Override public OWLIndividual serialize(Property property, OWLOntology ontology)
            throws ZoomaSerializationException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass propertyClass = factory.getOWLClass(propertyClassIRI);

        OWLAnnotationProperty propertyNameProperty = factory.getOWLAnnotationProperty(propertyNamePropertyIRI);
        OWLAnnotationProperty propertyValueProperty = factory.getOWLAnnotationProperty(propertyValuePropertyIRI);

        getLog().trace("Creating IRI for property owl individual: " + property.getURI());
        IRI propertyIRI = createIRI(property.getURI());
        getLog().trace("Creating property owl individual");
        OWLNamedIndividual propertyIndividual = factory.getOWLNamedIndividual(propertyIRI);
        getLog().trace("Created property owl individual");
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(propertyClass, propertyIndividual));

        String label = "";
        if (property instanceof TypedProperty) {
            String propertyType = ((TypedProperty) property).getPropertyType();
            label = propertyType + ":";
            manager.addAxiom(ontology,
                             factory.getOWLAnnotationAssertionAxiom(propertyNameProperty,
                                                                    propertyIRI,
                                                                    factory.getOWLLiteral(propertyType)));
        }
        String propertyValue = property.getPropertyValue();
        manager.addAxiom(ontology,
                         factory.getOWLAnnotationAssertionAxiom(propertyValueProperty,
                                                                propertyIRI,
                                                                factory.getOWLLiteral(propertyValue)));
        manager.addAxiom(ontology,
                         factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(),
                                                                propertyIRI,
                                                                factory.getOWLLiteral(label + propertyValue)));
        return propertyIndividual;
    }
}
