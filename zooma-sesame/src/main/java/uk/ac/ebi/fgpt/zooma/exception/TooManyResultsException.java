package uk.ac.ebi.fgpt.zooma.exception;

/**
 * Exception thrown whenever a query returns more results than expected.
 *
 * @author Simon Jupp
 * @date 21/05/2012 Functional Genomics Group EMBL-EBI
 */
public class TooManyResultsException extends RuntimeException {
    public TooManyResultsException() {
        super();
    }

    public TooManyResultsException(String s) {
        super(s);
    }

    public TooManyResultsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TooManyResultsException(Throwable throwable) {
        super(throwable);
    }
}
