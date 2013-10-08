package uk.ac.ebi.fgpt.zooma.exception;

/**
 * A runtime exception that is thrown if a query against a SPARQL repository fails
 *
 * @author Tony Burdett
 * @date 22/10/12
 */
public class SPARQLQueryException extends RuntimeException {
    public SPARQLQueryException(Exception e) {
        super(e);
    }

    public SPARQLQueryException(String s, Exception e) {
        super(s, e);
    }
}
