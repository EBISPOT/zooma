package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * A basic implementation of a Study
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class SimpleStudy extends AbstractIdentifiable implements Study {
    private static final long serialVersionUID = -5315117789939420035L;

    private String accession;

    public SimpleStudy(URI uri, String accession) {
        super(uri);
        this.accession = accession;
    }

    @Override public String getAccession() {
        return accession;
    }

    @Override public String toString() {
        return "SimpleStudy {\n" +
                "  uri='" + getURI() + "'\n" +
                "  accession='" + accession + "'\n}";
    }
}
