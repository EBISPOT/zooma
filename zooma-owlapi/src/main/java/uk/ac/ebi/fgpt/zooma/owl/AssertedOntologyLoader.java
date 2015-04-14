package uk.ac.ebi.fgpt.zooma.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Loads an ontology using the OWLAPI, and considers only axioms that are asserted in the loaded ontology when
 * generating class labels and types
 *
 * @author Tony Burdett
 * @author James Malone
 * @date 15/02/12
 */
public class AssertedOntologyLoader extends AbstractOntologyLoader {
    protected OWLOntology loadOntology() throws OWLOntologyCreationException {
        getLog().debug("Loading ontology...");
        OWLOntology ontology = getManager().loadOntology(IRI.create(getOntologyURI()));
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        setOntologyIRI(ontologyIRI);
        if (getOntologyName() == null) {
            String name = URIUtils.getShortform(ontologyIRI.toURI());
            if (name == null) {
                getLog().warn("Can't generate a short form for " + ontologyIRI.toString() +
                                      " - you should register this namespace in zooma/prefix.properties " +
                                      "to ensure ZOOMA can correctly shorten URIs in this namespace");
            }
            else {
                setOntologyName(name);
            }
        }
        getLog().debug("Successfully loaded ontology " + ontologyIRI);
        Set<OWLClass> allClasses = ontology.getClassesInSignature();
        Set<URI> allKnownNamespaces = new HashSet<>();

        // remove excluded classes from allClasses by subclass
        if (getExclusionClassURI() != null) {
            OWLClass excludeClass = getFactory().getOWLClass(IRI.create(getExclusionClassURI()));
            for (OWLClassExpression subClassExpression : excludeClass.getSubClasses(ontology)) {
                OWLClass subclass = subClassExpression.asOWLClass();
                allClasses.remove(subclass);
            }
        }

        // remove excluded classes from allClasses by annotation property
        if (getExclusionAnnotationURI() != null) {
            OWLAnnotationProperty excludeAnnotation =
                    getFactory().getOWLAnnotationProperty(IRI.create(getExclusionAnnotationURI()));
            Iterator<OWLClass> allClassesIt = allClasses.iterator();
            while (allClassesIt.hasNext()) {
                OWLClass owlClass = allClassesIt.next();
                if (!owlClass.getAnnotations(ontology, excludeAnnotation).isEmpty()) {
                    allClassesIt.remove();
                }
            }
        }

        OWLAnnotationProperty rdfsLabel = getFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        Collection<OWLAnnotationProperty> synonyms = new HashSet<OWLAnnotationProperty>();
        for (URI ap : getSynonymURIs()) {
            synonyms.add(getFactory().getOWLAnnotationProperty(IRI.create(ap)));
        }

        int labelCount = 0;
        int labelledClassCount = 0;
        int synonymCount = 0;
        int synonymedClassCount = 0;
        getLog().debug("Loading labels and synonyms...");
        for (OWLClass ontologyClass : allClasses) {
            IRI clsIri = ontologyClass.getIRI();

            // get namespace for this IRI
            URI namespace = URIUtils.extractNamespace(clsIri.toURI());
            if (!URIUtils.isNamespaceKnown(namespace)) {
                getLog().warn(
                        "Namespace <" + namespace + "> (present in ontology " + ontologyIRI.toString() + ") " +
                                "is not known - you should register this namespace in zooma/prefix.properties " +
                                "to ensure ZOOMA can correctly shorten URIs in this namespace");
            }
            else {
                allKnownNamespaces.add(namespace);
            }

            // get label annotations
            Set<String> labels = getStringLiteralAnnotationValues(ontology, ontologyClass, rdfsLabel);
            String label = null;
            if (labels.isEmpty()) {
                getLog().warn("OWLClass " + ontologyClass + " contains no label. " +
                                      "No labels for this class will be loaded.");
            }
            else {
                if (labels.size() > 1) {
                    getLog().warn("OWLClass " + ontologyClass + " contains more than one label " +
                                          "(including '" + labels.iterator().next() + "'). " +
                                          "No labels for this class will be loaded.");
                }
                else {
                    label = labels.iterator().next();
                    addClassLabel(clsIri, label);
                    labelledClassCount++;
                    labelCount++;
                }
            }

            // get types
            Set<String> ontologyTypeLabelSet = new HashSet<>();
            for (OWLClassExpression parentClassExpression : ontologyClass.getSuperClasses(ontology)) {
                if (!parentClassExpression.isAnonymous()) {
                    OWLClass parentClass = parentClassExpression.asOWLClass();
                    getLog().debug("Next parent of " + label + ": " + parentClass);
                    Set<String> typeVals = getStringLiteralAnnotationValues(ontology, parentClass, rdfsLabel);
                    ontologyTypeLabelSet.addAll(typeVals);
                }
                else {
                    getLog().trace("OWLClassExpression " + parentClassExpression + " is an anonymous class. " +
                                           "No synonyms for this class will be loaded.");
                }
            }
            addClassTypes(clsIri, ontologyTypeLabelSet);

            // get all synonym annotations
            for (OWLAnnotationProperty synonym : synonyms) {
                Set<String> synonymVals = getStringLiteralAnnotationValues(ontology, ontologyClass, synonym);
                if (synonymVals.isEmpty()) {
                    getLog().trace("OWLClass " + ontologyClass + " contains no synonyms. " +
                                           "No synonyms for this class will be loaded.");
                }
                else {
                    addSynonyms(clsIri, synonymVals);
                    synonymCount += synonymVals.size();
                    synonymedClassCount++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (URI namespace : allKnownNamespaces) {
            sb.append("\t").append(namespace.toString()).append("\n");
        }

        getLog().debug("Loaded classes with " + allKnownNamespaces.size() + " different namespaces " +
                               "from " + ontologyIRI.toString() + ". Those namespaces are...\n" + sb.toString());

        getLog().debug("Successfully loaded " + labelCount + " labels on " + labelledClassCount + " classes and " +
                               synonymCount + " synonyms on " + synonymedClassCount + " classes!");

        return ontology;
    }
}
