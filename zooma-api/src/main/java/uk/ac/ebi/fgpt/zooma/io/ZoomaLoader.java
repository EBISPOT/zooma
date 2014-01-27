package uk.ac.ebi.fgpt.zooma.io;

import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.model.Update;

import java.util.Collection;

/**
 * Loads a collection of data objects into ZOOMA.  This is a higher level interface that acts as an abstraction over
 * more granular IO operations performed by classes in the {@link uk.ac.ebi.fgpt.zooma.io} package.  Typically, loaders
 * will make use of the {@link uk.ac.ebi.fgpt.zooma .io.ZoomaResolver}, {@link uk.ac.ebi.fgpt.zooma.io.ZoomaSerializer}
 * and {@link uk.ac.ebi.fgpt.zooma.io .ZoomaStorer} interfaces to compose a complete load operation.
 * <p/>
 * All load operations should be atomic.  Any resources or temporary files should be cleaned up after loading has
 * completed (either successfully or otherwise).
 *
 * @author Tony Burdett
 * @date 11/06/13
 */
@Deprecated
public interface ZoomaLoader<T> {
    /**
     * Loads the collection of supplied zooma objects into ZOOMA by resolving, serializing and storing (as required by
     * the implementation)
     *
     * @param datasourceName the name of the datasource the objects to load came from (or some other name to use in
     *                       logging for identification purposes)
     * @param zoomaObjects   the collection of objects being loaded
     * @throws ZoomaLoadingException
     */
    @Deprecated
    void load(String datasourceName, Collection<T> zoomaObjects) throws ZoomaLoadingException;

    /**
     * Loads the supplied zooma object into ZOOMA by resolving, serializing and storing (as required by the
     * implementation)
     *
     * @param zoomaObject the object being loaded
     * @throws ZoomaLoadingException
     */
    @Deprecated
    void load(T zoomaObject) throws ZoomaLoadingException;

    /**
     * Updtaes the supplied zooma objects into ZOOMA by resolving, serializing and storing (as required by the
     * implementation)
     *
     * @param zoomaObject the object being loaded
     * @throws ZoomaLoadingException
     */
    void update(Collection<T> zoomaObject, Update<T> update) throws ZoomaLoadingException;
}
