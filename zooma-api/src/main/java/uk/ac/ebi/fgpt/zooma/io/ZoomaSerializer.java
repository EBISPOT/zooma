package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Serializes ZOOMA model objects to a serialized from which can be persisted to a file or output stream.
 * <p/>
 * Collections of ZOOMA model objects are serialized to a file or output stream, making use of container.  A container
 * is the memory-based model for the serialized representation and is chosen based on the API being used for
 * serialization.  A container may be the OWL-API, a Jena graph, and XML DOM, or something else, and is specified by the
 * type parameter <code>&lt;C&gt;</code>
 * <p/>
 * Individual zooma objects can be serialized one at a time into the container using this class.  This gives you
 * complete control over when to create the container and which objects you would like to serialize to it,  It also
 * allows for customized serialization strategies which write several objects of different types into a single
 * container.  Individual model objects are serialized to the serialized form, the type of which is specified by the
 * type parameter <code>&lt;S&gt;</code>
 *
 * @param <T> the type of model objects to serialize
 * @param <C> the container to use to serialize batches of model objects
 * @param <S> the type of the serialized form of an individual object (e.g. and OWL-API individual or a DOM node)
 * @author Tony Burdett
 * @date 25/09/12
 */
public interface ZoomaSerializer<T, C, S> {
    /**
     * Generates a serialized representation of the collection of supplied zooma objects and writes it to the supplied
     * File
     *
     * @param datasourceName the name of the datasource the objects to serialize came from (or some other name to use in
     *                       logging for identification purposes)
     * @param zoomaObjects   the collection of objects being serialized
     * @param out            the file to save the serialized representation to
     */
    void serialize(String datasourceName, Collection<T> zoomaObjects, File out)
            throws ZoomaSerializationException;

    /**
     * Generates a serialized representation of the collection of supplied zooma objects and writes it to the supplied
     * output stream
     *
     * @param datasourceName the name of the datasource the objects to serialize came from (or some other name to use in
     *                       logging for identification purposes)
     * @param zoomaObjects   the collection of objects being serialized
     * @param out            the output stream to save the serialized representation to
     */
    void serialize(String datasourceName, Collection<T> zoomaObjects, OutputStream out)
            throws ZoomaSerializationException;

    /**
     * Generates a serialized representation of a single supplied zooma object and stores it in the supplied container
     */
    S serialize(T zoomaObject, C container) throws ZoomaSerializationException;
}
