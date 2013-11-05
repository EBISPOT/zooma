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
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.springframework.util.StringUtils;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A ZOOMA serializer that uses the OWL API to generate RDF from annotations
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 04/10/12
 */
public class OWLAPIAnnotationSerializer extends OWLAPIZoomaSerializer<Annotation> {
    private final IRI annotationClassIRI = IRI.create(Namespaces.OAC.getURI() + "DataAnnotation");

    private final IRI hasBodyPropertyIRI = IRI.create(Namespaces.OAC.getURI() + "hasBody");
    private final IRI hasTargetPropertyIRI = IRI.create(Namespaces.OAC.getURI() + "hasTarget");
    private final IRI semanticTagClassIRI = IRI.create(Namespaces.OAC.getURI() + "SemanticTag");

    private final IRI replacesPropertyIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "replaces");
    private final IRI replacedByPropertyIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "isReplacedBy");

    private final IRI hasDBPropertyIRI = IRI.create(Namespaces.DC.getURI() + "source");
    private final IRI hasEvidencePropertyIRI = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "hasEvidence");
    private final IRI hasShortName = IRI.create(Namespaces.ZOOMA_TERMS.getURI() + "shortName");
    private final IRI generationDatePropertyIRI = IRI.create(Namespaces.OAC.getURI() + "generated");
    private final IRI generatorPropertyIRI = IRI.create(Namespaces.OAC.getURI() + "generator");

    private final IRI annotatedDatePropertyIRI = IRI.create(Namespaces.OAC.getURI() + "annotated");
    private final IRI annotatorPropertyIRI = IRI.create(Namespaces.OAC.getURI() + "annotator");

    private final OWLAPIPropertySerializer propertySerializer;
    private final OWLAPIBiologicalEntitySerializer biologicalEntitySerializer;

    public OWLAPIAnnotationSerializer() {
        this(new RDFXMLOntologyFormat());
    }

    public OWLAPIAnnotationSerializer(OWLOntologyFormat outputFormat) {
        this(outputFormat,
                new OWLAPIPropertySerializer(outputFormat),
                new OWLAPIBiologicalEntitySerializer(outputFormat));
    }

    public OWLAPIAnnotationSerializer(OWLOntologyFormat outputFormat,
                                      OWLAPIPropertySerializer propertySerializer,
                                      OWLAPIBiologicalEntitySerializer biologicalEntitySerializer) {
        super(outputFormat);
        this.propertySerializer = propertySerializer;
        this.biologicalEntitySerializer = biologicalEntitySerializer;
    }

    public OWLAPIPropertySerializer getPropertySerializer() {
        return propertySerializer;
    }

    public OWLAPIBiologicalEntitySerializer getBiologicalEntitySerializer() {
        return biologicalEntitySerializer;
    }

    @Override public OWLIndividual serialize(Annotation annotation, OWLOntology ontology)
            throws ZoomaSerializationException {
        // convert the annotation
        OWLIndividual annotationIndividual = convertAnnotation(annotation, ontology);

        // convert the annotated property
        OWLIndividual propertyIndividual = getPropertySerializer().serialize(annotation.getAnnotatedProperty(),
                ontology);



        // associate annotation and property
        assertAnnotationHasBody(annotationIndividual, propertyIndividual, ontology);

        for (BiologicalEntity biologicalEntity : annotation.getAnnotatedBiologicalEntities()) {
            // convert biological entities
            OWLIndividual biologicalEntityIndividual = getBiologicalEntitySerializer().serialize(biologicalEntity,
                    ontology);

            // associate annotation and biological entity
            assertAnnotationHasTarget(annotationIndividual, biologicalEntityIndividual, ontology);
        }

        for (URI semanticTag : annotation.getSemanticTags()) {
            // convert semantic tag
            OWLIndividual semanticTagIndividual = convertSemanticTag(semanticTag, ontology);

            // associate annotation and semantic tag
            if (semanticTagIndividual != null) {
                assertAnnotationHasTag(annotationIndividual, semanticTagIndividual, ontology);
            }
            else {
                getLog().trace("The semantic tag for annotation '" + annotation + "' was null," +
                        " and will not annotated to anything in ZOOMA");
            }
        }

        for (URI replaces : annotation.replaces()) {
            // convert replaces annotation
            OWLIndividual replacesIndividual = convertAnnotationByURI(replaces, ontology);

            // associate this annotation with those it replaces
            assertAnnotationReplaces(annotationIndividual, replacesIndividual, ontology);
        }

        for (URI replacedBy : annotation.replacedBy()) {
            // convert replaced by annotation
            OWLIndividual replacedByIndividual = convertAnnotationByURI(replacedBy, ontology);

            // associate this annotation with those it was replaced by
            assertAnnotationReplacedBy(annotationIndividual, replacedByIndividual, ontology);
        }

        // associate provenance
        assertAnnotationProvenance(annotationIndividual,
                annotation.getProvenance(),
                ontology);

        return annotationIndividual;
    }

    private String getAnnotationLabel(Property annotatedProperty, Collection<BiologicalEntity> annotatedBiologicalEntities, Collection<URI> semanticTags) {

        StringBuilder sb = new StringBuilder();

        String pts = "";
        if (annotatedProperty instanceof TypedProperty) {
            pts = ((TypedProperty) annotatedProperty).getPropertyType() + ":";
        }
        pts += annotatedProperty.getPropertyValue();
        sb.append(pts);
        sb.append(" annotated to ");

        if (!semanticTags.isEmpty()) {
            List<String> tagUris = new ArrayList<String>();
            for (URI u : semanticTags) {
                tagUris.add(URIUtils.extractFragment(u));
            }
            sb.append(StringUtils.collectionToCommaDelimitedString(tagUris));
        }

        if (!annotatedBiologicalEntities.isEmpty()) {
            List<String> bes = new ArrayList<String>();
            for (BiologicalEntity be : annotatedBiologicalEntities) {
                bes.add(be.getName());
            }
            sb.append(" in ");
            sb.append(StringUtils.collectionToCommaDelimitedString(bes));
        }
        return sb.toString();
    }

    private OWLIndividual convertAnnotation(Annotation annotation, OWLOntology ontology) {
        getLog().trace("Converting annotation '" + annotation + "' to owl instance");
        OWLIndividual annotationInstance = convertAnnotationByURI(annotation.getURI(), ontology);
        // set annotation label
        String annotationLabel = getAnnotationLabel (annotation.getAnnotatedProperty(), annotation.getAnnotatedBiologicalEntities(), annotation.getSemanticTags());
        if (!"".equals(annotationLabel)) {
            ontology.getOWLOntologyManager().addAxiom(ontology,
                    ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(
                            ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSLabel(),
                            annotationInstance.asOWLNamedIndividual().getIRI(),
                            ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(annotationLabel)));
        }
        return annotationInstance;
    }

    private OWLIndividual convertAnnotationByURI(URI annotationURI, OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass dataAnnotationClass = factory.getOWLClass(annotationClassIRI);

        getLog().trace("Creating IRI for annotation owl individual: " + annotationURI);
        IRI annotationIRI = createIRI(annotationURI);
        getLog().trace("Creating annotation owl individual");
        OWLNamedIndividual annotationInstance = factory.getOWLNamedIndividual(annotationIRI);
        getLog().trace("Created annotation owl individual");
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(dataAnnotationClass, annotationInstance));
        return annotationInstance;
    }

    private OWLIndividual convertSemanticTag(URI semanticTag, OWLOntology ontology) {
        if (semanticTag != null) {
            getLog().trace("Converting semantic tag '" + semanticTag + "' to owl instance");
            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();

            getLog().trace("Creating IRI for semantic tag owl individual: " + semanticTag);
            IRI semanticTagIRI = createIRI(semanticTag);
            OWLClass ontologyClass = factory.getOWLClass(semanticTagClassIRI);
            getLog().trace("Creating semantic tag owl individual");
            OWLNamedIndividual semanticTagInstance = factory.getOWLNamedIndividual(semanticTagIRI);
            getLog().trace("Created semantic tag owl individual");
            manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(ontologyClass, semanticTagInstance));
            return semanticTagInstance;
        }
        else {
            return null;
        }
    }

    private void assertAnnotationHasBody(OWLIndividual annotationIndividual,
                                         OWLIndividual propertyIndividual,
                                         OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty hasBodyProperty = factory.getOWLObjectProperty(hasBodyPropertyIRI);

        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(hasBodyProperty,
                        annotationIndividual,
                        propertyIndividual));
    }

    private void assertAnnotationHasTag(OWLIndividual annotationIndividual,
                                        OWLIndividual semanticTagIndividual,
                                        OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty hasTagProperty = factory.getOWLObjectProperty(hasBodyPropertyIRI);

        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(hasTagProperty,
                        annotationIndividual,
                        semanticTagIndividual));
    }

    private void assertAnnotationHasTarget(OWLIndividual annotationIndividual,
                                           OWLIndividual biologicalEntityIndividual,
                                           OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty hasTargetProperty = factory.getOWLObjectProperty(hasTargetPropertyIRI);

        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(hasTargetProperty,
                        annotationIndividual,
                        biologicalEntityIndividual));

    }

    private void assertAnnotationReplaces(OWLIndividual annotation,
                                          OWLIndividual replacedAnnotation,
                                          OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty replacesProperty = factory.getOWLObjectProperty(replacesPropertyIRI);

        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(replacesProperty,
                        annotation,
                        replacedAnnotation));
    }

    private void assertAnnotationReplacedBy(OWLIndividual annotation,
                                            OWLIndividual replaceByAnnotation,
                                            OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty replacedByProperty = factory.getOWLObjectProperty(replacedByPropertyIRI);

        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(replacedByProperty,
                        annotation,
                        replaceByAnnotation));
    }

    // localized caches to avoid repeatedly creating the same IRIs over and over
    private final Map<URI, IRI> sourceProvenanceIRIMap = new HashMap<>();
    private final Map<String, IRI> evidenceProvenanceIRIMap = new HashMap<>();

    private void assertAnnotationProvenance(OWLIndividual annotationInstance,
                                            AnnotationProvenance provenance,
                                            OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLObjectProperty hasDBProperty = factory.getOWLObjectProperty(hasDBPropertyIRI);
        OWLObjectProperty hasEvidenceProperty = factory.getOWLObjectProperty(hasEvidencePropertyIRI);

        // get datasource IRI
        IRI datasourceIRI;
        IRI datasourceTypeIRI;
        synchronized (sourceProvenanceIRIMap) {
            if (!sourceProvenanceIRIMap.containsKey(provenance.getSource().getURI())) {
                datasourceIRI = createIRI(provenance.getSource().getURI());
                sourceProvenanceIRIMap.put(provenance.getSource().getURI(), datasourceIRI);
            }
            else {
                datasourceIRI = sourceProvenanceIRIMap.get(provenance.getSource().getURI());
            }
            datasourceTypeIRI =  IRI.create(URIBindingUtils.getURI(provenance.getSource().getType().name()));
        }

        // get evidence IRI
        IRI evidenceIRI;
        synchronized (evidenceProvenanceIRIMap) {
            if (!evidenceProvenanceIRIMap.containsKey(provenance.getEvidence().toString())) {
                evidenceIRI = createIRI(
                        URIBindingUtils.getURI(provenance.getEvidence().name()));
                evidenceProvenanceIRIMap.put(provenance.getEvidence().toString(), evidenceIRI);
            }
            else {
                evidenceIRI = evidenceProvenanceIRIMap.get(provenance.getEvidence().toString());
            }
        }

        OWLNamedIndividual datasourceIndividual = factory.getOWLNamedIndividual(datasourceIRI);
        OWLClass datasourceTypeClass = factory.getOWLClass(datasourceTypeIRI);
        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(hasDBProperty,
                        annotationInstance,
                        datasourceIndividual));

        manager.addAxiom(ontology,
                factory.getOWLClassAssertionAxiom(datasourceTypeClass, datasourceIndividual));

        String shortName = null;
        // set shortName
        if (provenance.getSource().getName() != null) {
            shortName = provenance.getSource().getName();
        }
        else {
            shortName = URIUtils.getShortform(provenance.getSource().getURI());
            if (shortName == null ) {
                shortName = provenance.getSource().getURI().toString();
            }
        }
        manager.addAxiom(ontology,
                factory.getOWLAnnotationAssertionAxiom(
                        factory.getOWLAnnotationProperty(hasShortName),
                        datasourceIRI,
                        factory.getOWLLiteral(shortName)));

        // set evidence
        OWLNamedIndividual evidenceIndividual = factory.getOWLNamedIndividual(evidenceIRI);
        manager.addAxiom(ontology,
                factory.getOWLObjectPropertyAssertionAxiom(hasEvidenceProperty,
                        annotationInstance,
                        evidenceIndividual));

        // set annotation generation data
        OWLAnnotationProperty generationDateProperty = factory.getOWLAnnotationProperty(generationDatePropertyIRI);
        OWLAnnotationProperty generatedProperty = factory.getOWLAnnotationProperty(generatorPropertyIRI);

        // set generated date
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        manager.addAxiom(ontology,
                factory.getOWLAnnotationAssertionAxiom(
                        generationDateProperty,
                        annotationInstance.asOWLNamedIndividual().getIRI(),
                        factory.getOWLLiteral(dateformat.format(provenance.getGeneratedDate()),
                                OWL2Datatype.XSD_DATE_TIME)));

        // set generator
        manager.addAxiom(ontology,
                factory.getOWLAnnotationAssertionAxiom(
                        generatedProperty,
                        annotationInstance.asOWLNamedIndividual().getIRI(),
                        factory.getOWLLiteral(provenance.getGenerator())));


        // set annotation provence if available
        OWLAnnotationProperty annotatatedDateProperty = factory.getOWLAnnotationProperty(annotatedDatePropertyIRI);
        OWLAnnotationProperty annotatorProperty = factory.getOWLAnnotationProperty(annotatorPropertyIRI);

        if (provenance.getAnnotationDate() != null) {
            // set generated date
            manager.addAxiom(ontology,
                    factory.getOWLAnnotationAssertionAxiom(
                            annotatatedDateProperty,
                            annotationInstance.asOWLNamedIndividual().getIRI(),
                            factory.getOWLLiteral(dateformat.format(provenance.getAnnotationDate()),
                                    OWL2Datatype.XSD_DATE_TIME)));
        }

        if (provenance.getAnnotator() != null) {
            // set generator
            manager.addAxiom(ontology,
                    factory.getOWLAnnotationAssertionAxiom(
                            annotatorProperty,
                            annotationInstance.asOWLNamedIndividual().getIRI(),
                            factory.getOWLLiteral(provenance.getAnnotator())));
        }
    }
}
