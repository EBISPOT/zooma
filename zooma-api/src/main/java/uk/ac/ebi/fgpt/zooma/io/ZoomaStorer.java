package uk.ac.ebi.fgpt.zooma.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Takes a serialized representation of ZOOMA model objects and stores them in a datasource.  How this storage is
 * accomplished is dependent on the implementation.
 *
 * @author Tony Burdett
 * @date 06/06/13
 */
public interface ZoomaStorer {
    /**
     * Takes a serialized collection of ZOOMA model objects in the supplied file and stores them in the underlying
     * datasource
     *
     * @param serializedObjects the collections of serialized objects to store
     * @throws IOException if there was a problem reading from the file or storing the serialized objects in the
     *                     underlying datasource
     */
    void store(File serializedObjects) throws IOException;

    /**
     * Takes a serialized collection of ZOOMA model objects from the supplied input stream and stores them in the
     * underlying datasource
     *
     * @param serializedObjects the collections of serialized objects to store
     * @throws IOException if there was a problem reading from the input stream or storing the serialized objects in the
     *                     underlying datasource
     */
    void store(InputStream serializedObjects) throws IOException;
}
