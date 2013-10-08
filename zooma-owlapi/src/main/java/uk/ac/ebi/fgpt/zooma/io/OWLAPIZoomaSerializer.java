package uk.ac.ebi.fgpt.zooma.io;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;

/**
 * An abstract implementation of a {@link ZoomaSerializer} for the OWLAPI.  This implementation specifies that the
 * container for serialized objects is the OWL-API {@link OWLOntology} object, and that each zooma object should be
 * serialized into an {@link OWLIndividual} instance.
 * <p/>
 * This class defines the general collection serialization methods, as well as some utilities for working with the
 * OWL-API for writing to files and streams and for converting URIs to IRIs
 *
 * @author Tony Burdett
 * @date 07/08/13
 */
public abstract class OWLAPIZoomaSerializer<T> implements ZoomaSerializer<T, OWLOntology, OWLIndividual> {
    private final OWLOntologyFormat outputFormat;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public OWLAPIZoomaSerializer() {
        this(new RDFXMLOntologyFormat());
    }

    public OWLAPIZoomaSerializer(OWLOntologyFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override public void serialize(String datasourceName,
                                    Collection<T> zoomaObjects,
                                    File file)
            throws ZoomaSerializationException {
        if (zoomaObjects.size() > 0) {
            T zoomaObject = zoomaObjects.iterator().next();
            getLog().info("Serializing " + zoomaObjects.size() + " " + zoomaObject.getClass().getSimpleName() +
                                  " objects to " + file.getAbsolutePath());
        }
        OutputStream out = null;
        try {
            if (!file.getAbsoluteFile().getParentFile().exists()) {
                if (!file.getAbsoluteFile().getParentFile().mkdirs()) {
                    throw new ZoomaSerializationException(
                            "Unable to create directory '" + file.getParentFile().getAbsolutePath() + "'");
                }
            }
            out = new BufferedOutputStream(new FileOutputStream(file));
            serialize(datasourceName, zoomaObjects, out);
        }
        catch (IOException e) {
            throw new ZoomaSerializationException("Unable to write to '" + file + "'", e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    // tried our best!
                    getLog().error("Failed to close a stream for serializing annotations to " + file.getAbsolutePath());
                }
            }
        }
    }

    @Override public void serialize(String datasourceName, Collection<T> zoomaObjects, OutputStream out)
            throws ZoomaSerializationException {
        IRI ontologyIRI = createIRI(URI.create(
                Namespaces.ZOOMA.getURI() + datasourceName + "/dataset/" + System.currentTimeMillis()));
        getLog().debug("URI for newly created annotation dataset is '" + ontologyIRI.toString() + "'");
        try {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology(ontologyIRI);
            for (T zoomaObject : zoomaObjects) {
                serialize(zoomaObject, ontology);
            }
            saveOntology(ontology, out);
        }
        catch (OWLOntologyStorageException e) {
            throw new ZoomaSerializationException(
                    "Failed to store data in ontology '" + ontologyIRI.toString() + "'", e);
        }
        catch (OWLOntologyCreationException e) {
            throw new ZoomaSerializationException("Failed to create ontology '" + ontologyIRI.toString() + "'", e);
        }
    }

    /**
     * Synchronized create method, to try to protected against limitations with OWLAPI IRI.create() lacking
     * thread-safety
     *
     * @param uri the URI to wrap as an IRI
     * @return an IRI
     */
    protected synchronized IRI createIRI(URI uri) {
        return IRI.create(uri);
    }

    /**
     * Saves the supplies OWLOntology to the given stream, using the configured OWLOntologyFormat
     *
     * @param ontology the ontology to save
     * @param out      the stream to save the ontology to
     * @throws OWLOntologyStorageException
     */
    protected void saveOntology(OWLOntology ontology, OutputStream out) throws OWLOntologyStorageException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        getLog().debug("Saving ontology using '" + outputFormat.toString() + "' output format");
        manager.saveOntology(ontology, outputFormat, out);
    }
}
