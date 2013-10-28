package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation of a Study
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class SimpleStudy extends AbstractIdentifiable implements Study {
    private static final long serialVersionUID = -5315117789939420035L;

    private String accession;

    private Set<URI> types;

    public SimpleStudy(URI uri, String accession) {
        super(uri);
        this.accession = accession;
        this.types = new HashSet<URI>();
    }

    @Override public String getAccession() {
        return accession;
    }

    @Override public String toString() {
        return "SimpleStudy {\n" +
                "  uri='" + getURI() + "'\n" +
                "  accession='" + accession + "'\n}";
    }

    @Override
    public Set<URI> getTypes() {
        return types;
    }
}
