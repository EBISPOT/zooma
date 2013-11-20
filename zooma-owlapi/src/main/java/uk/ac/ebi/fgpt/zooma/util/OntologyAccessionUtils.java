package uk.ac.ebi.fgpt.zooma.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains some utilities for working with ontology "accessions"
 *
 * @author Tony Burdett
 * @date 04/10/12
 */
public class OntologyAccessionUtils {
    private static final Set<URL> loadedURLs = Collections.synchronizedSet(new HashSet<URL>());
    private static final Map<String, IRI> accessionToIRIMap = Collections.synchronizedMap(new HashMap<String, IRI>());
    private static final Map<IRI, String> iriToAccessionMap = Collections.synchronizedMap(new HashMap<IRI, String>());

    private static final Logger log = LoggerFactory.getLogger(OntologyAccessionUtils.class);

    protected static Logger getLog() {
        return log;
    }

    public synchronized static void loadOntology(URL ontologyURL) throws IOException {
        if (loadedURLs.contains(ontologyURL)) {
            // already loaded once, swallow
            getLog().debug("Accessions from " + ontologyURL + " have already been loaded");
        }
        else {
            getLog().info("Downloading and loading ontology from " + ontologyURL);
            loadOntology(ontologyURL.openStream());
            loadedURLs.add(ontologyURL);
        }
    }

    public synchronized static void loadOntology(InputStream ontologySource) throws IOException {
        try {
            // load ontology from source
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologySource);

            // for each class, extract "accession"
            getLog().info("Initialising accession -> IRI ontology mapping for fast lookup by ID");
            int count = 0;
            for (OWLClass cls : ontology.getClassesInSignature()) {
                getLog().debug("Next class: " + cls + " (done " + count + " now)");
                String shortform = getAccessionFromIRI(cls.getIRI());
                if (accessionToIRIMap.containsKey(shortform)) {
                    if (accessionToIRIMap.get(shortform).equals(cls.getIRI())) {
                        getLog().debug("Accession '" + shortform + "' is already mapped to " + cls.getIRI());
                    }
                    else {
                        getLog().debug("Accession '" + shortform + "' cannot be mapped to " + cls.getIRI() + ", " +
                                               "conflict with " + accessionToIRIMap.get(shortform));
                    }
                }
                else {
                    accessionToIRIMap.put(shortform, cls.getIRI());
                    iriToAccessionMap.put(cls.getIRI(), shortform);
                    count++;
                    getLog().debug("Stored accession mapping: " + shortform + " -> " + cls.getIRI());
                }
            }
            if (count > 0) {
                getLog().info("Loaded " + count + " new accession-IRI mappings to ontology utils");
            }
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Failed to read an ontology from the supplied input stream", e);
        }
    }

    public synchronized static IRI getIRIFromAccession(String accession) {
        if (!accessionToIRIMap.containsKey(accession)) {
            // attempt lookup in IRI set - maybe we got an IRI as an accession?
            try {
                IRI iri = IRI.create(accession);
                if (iriToAccessionMap.containsKey(iri)) {
                    return iri;
                }
            }
            catch (Exception e) {
                // can safely ignore this
                getLog().debug("Caught IRI creation exception", e);
            }
            getLog().warn("Unable to identify IRI for term accession '" + accession + "'.  " +
                                  "This accession has been blacklisted and will resolve to null.");
            accessionToIRIMap.put(accession, null);
        }
        return accessionToIRIMap.get(accession);
    }

    public static String getAccessionFromIRI(IRI iri) {
        return getAccessionFromURI(iri.toURI());
    }

    public static String getAccessionFromURI(URI uri) {
        String termURI = uri.toString();

        // try handling old-style chebi uris
        if (termURI.contains("http://www.ebi.ac.uk/chebi/searchId")) {
            return termURI.substring(termURI.lastIndexOf("=") + 1);
        }

        if (uri.getFragment() != null) {
            // get URI fragment if possible
            return uri.getFragment();
        }
        else {
            // return everything after last '/'
            return uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        }
    }
}
