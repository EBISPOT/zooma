package uk.ac.ebi.spot.zooma.exception;

/**
 * An exception thrown when a data supplied to ZOOMA does not conform to the format prescribed in the javadocs of the
 * service.
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public class InvalidDataFormatException extends RuntimeException {
    public InvalidDataFormatException(String message) {
        super(message);
    }
}