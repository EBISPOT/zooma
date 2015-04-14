package uk.ac.ebi.fgpt.zooma.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A temporary placeholder implementation of a {@link ZoomaStorer}.  Methods are unimplemented and throw exceptions, but
 * this allows end to end testing of the rest of the infrastructure
 *
 * @author Tony Burdett
 * @date 11/06/13
 */
@Deprecated
public class UnimplementedZoomaStorer implements ZoomaStorer {
    @Override
    public void store(File serializedObjects) throws IOException {
        throw new UnsupportedOperationException(
                "Cannot store data from file '" + serializedObjects.getAbsolutePath() + "': " +
                        "this method is not yet implemented");
    }

    @Override
    public void store(InputStream serializedObjects) throws IOException {
        throw new UnsupportedOperationException(
                "Cannot store data from stream '" + serializedObjects.toString() + "': " +
                        "this method is not yet implemented");
    }
}
