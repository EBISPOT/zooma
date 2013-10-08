package uk.ac.ebi.fgpt.zooma.exception;

/**
 * A runtime exception that is thrown if a connection to a RDF repository fails
 *
 * @author Simon Jupp
 * @date 21/06/2012 Functional Genomics Group EMBL-EBI
 */
public class RDFRepositoryException extends RuntimeException {
    public RDFRepositoryException(Exception e) {
        super(e);
    }

    public RDFRepositoryException(String s, Exception e) {
        super(s, e);
    }
}
