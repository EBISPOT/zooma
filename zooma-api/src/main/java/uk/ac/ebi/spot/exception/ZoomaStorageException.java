package uk.ac.ebi.spot.exception;

import java.io.IOException;

/**
 * An exception thrown whenever a serialized set of model objects from ZOOMA could not be stored in the underlying
 * storage medium
 *
 * @author Tony Burdett
 * @date 06/06/13
 */
public class ZoomaStorageException extends IOException {
    public ZoomaStorageException() {
        super();
    }

    public ZoomaStorageException(String message) {
        super(message);
    }

    public ZoomaStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoomaStorageException(Throwable cause) {
        super(cause);
    }
}
