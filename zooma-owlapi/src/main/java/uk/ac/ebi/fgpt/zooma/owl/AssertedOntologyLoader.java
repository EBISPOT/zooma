package uk.ac.ebi.fgpt.zooma.owl;

import com.google.common.base.Optional;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.*;

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
        int missingLabelsCount = 0;
        boolean hasMissingLabels = false;

        getLog().debug("Loading ontology...");
        OWLOntology ontology = getManager().loadOntology(IRI.create(getOntologyURI()));
        Optional<IRI> ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        setOntologyIRI(ontologyIRI);
        if (getOntologyName() == null) {
            String name = URIUtils.getShortform(ontologyIRI.get().toURI());
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
//      Before :  Set<OWLClass> allClasses = ontology.getClassesInSignature(false);
//      Now :
        Set<OWLClass> allClasses = ontology.getClassesInSignature();

        Set<URI> allObservedNamespaces = new HashSet<>();



        // remove excluded classes from allClasses by subclass
        if (getExclusionClassURI() != null) {
            OWLClass excludeClass = getFactory().getOWLClass(IRI.create(getExclusionClassURI()));

            // Before : for (OWLClassExpression subClassExpression : excludeClass.getSubClasses(ontology)) {
            //Now :
            Set<OWLSubClassOfAxiom> subclassAxioms = ontology.getSubClassAxiomsForSuperClass(excludeClass);
            Collection<OWLClassExpression> subClasses = new ArrayList<>();
            for(OWLSubClassOfAxiom subClassAxiom : subclassAxioms){
                subClasses.add(subClassAxiom.getSubClass());
            }

            for (OWLClassExpression subClassExpression :subClasses) {
                OWLClass subclass = subClassExpression.asOWLClass();
                allClasses.remove(subclass);
            }
        }

        // remove excluded classes from allClasses by annotation property
        if (getExclusionAnnotationURI() != null) {
            OWLAnnotationProperty excludeAnnotation = getFactory().getOWLAnnotationProperty(IRI.create(getExclusionAnnotationURI()));
            Iterator<OWLClass> allClassesIt = allClasses.iterator();
            while (allClassesIt.hasNext()) {
                OWLClass owlClass = allClassesIt.next();
//                if (!owlClass.getAnnotations(ontology, excludeAnnotation).isEmpty()) {
                if (! ontology.getAnnotationAssertionAxioms(owlClass.getIRI()).isEmpty()) {
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
            if (!URIUtils.isNamespaceKnown(namespace) && !allObservedNamespaces.contains(namespace)) {
                getLog().warn(
                        "Namespace <" + namespace + "> (present in ontology " + ontologyIRI.toString() + ") " +
                                "is not known - you should register this namespace in zooma/prefix.properties " +
                                "to ensure ZOOMA can correctly shorten URIs in this namespace");
            }
            allObservedNamespaces.add(namespace);

            // get label annotations
            Set<String> labels = getStringLiteralAnnotationValues(ontology, ontologyClass, rdfsLabel);
            String label = null;
            if (labels.isEmpty()) {
                getLog().trace("OWLClass " + ontologyClass + " contains no label. " +
                                       "No labels for this class will be loaded.");
                hasMissingLabels = true;
                missingLabelsCount++;
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

//            // get types
//            Set<String> ontologyTypeLabelSet = new HashSet<>();
//
//            Set<OWLSubClassOfAxiom> subClassAxioms = ontology.getSubClassAxiomsForSuperClass(ontologyClass);
//            Set<OWLClassExpression> superClasses = new HashSet<>();
//            for(OWLSubClassOfAxiom subClassAxiom : subClassAxioms){
//                superClasses.add(subClassAxiom.getSuperClass());
//
//            }



            //Before :  for (OWLClassExpression parentClassExpression : ontologyClass.getSuperClasses(ontology)) {
            //Now :
            Set<OWLSubClassOfAxiom> subClassAxioms = ontology.getSubClassAxiomsForSuperClass(ontologyClass);
            Set<OWLClassExpression> superClasses = new HashSet<>();
            for(OWLSubClassOfAxiom subClassAxiom : subClassAxioms){
                superClasses.add(subClassAxiom.getSuperClass());

            }
            Set<String> ontologyTypeLabelSet = new HashSet<>();

            for (OWLClassExpression parentClassExpression : superClasses) {
            // end now
                if (!parentClassExpression.isAnonymous()) {
                    OWLClass parentClass = parentClassExpression.asOWLClass();
                    getLog().trace("Next parent of " + label + ": " + parentClass);
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
        for (URI namespace : allObservedNamespaces) {
            sb.append("\t").append(namespace.toString()).append("\n");
        }

        if (hasMissingLabels) {
            getLog().warn("Some classes in ontology " + ontologyIRI.toString() + " have missing/multiple " +
                                  "labels and could not be loaded. This problem affected " +
                                  missingLabelsCount + "/" + allClasses.size() + " classes.");
        }

        getLog().debug("Loaded classes with " + allObservedNamespaces.size() + " different namespaces " +
                               "from " + ontologyIRI.toString() + ". Those namespaces are...\n" + sb.toString());

        getLog().debug("Successfully loaded " + labelCount + " labels on " + labelledClassCount + " classes and " +
                synonymCount + " synonyms on " + synonymedClassCount + " classes " +
                "from " + ontologyIRI.toString() + ".");

        return ontology;
    }

    public static void main(String[] args) throws OWLOntologyCreationException {
//        OWLManager.createOWLOntologyManager().loadOntology(IRI.create("http://purl.obolibrary.org/obo/hp.owl"));
        OWLManager.createOWLOntologyManager().loadOntology(IRI.create("http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/hp/hp.owl"));
    }


}
