package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

/**
 * A basic implementation of a Study
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 10/04/12
 */
public class SimpleStudy extends AbstractIdentifiable implements Study {
    private static final long serialVersionUID = -5315117789939420035L;

    private String accession;

    private Collection<URI> types;

    public SimpleStudy(URI uri, String accession) {
        this(uri, accession, new HashSet<URI>());
    }

    public SimpleStudy(URI uri, String accession, URI type) {
        super(uri);
        this.accession = accession;
        this.types = new HashSet<URI>();
        if (type != null) {
            getTypes().add(type);
        }
    }

    public SimpleStudy(URI uri, String accession, Collection<URI> types) {
        super(uri);
        this.accession = accession;
        this.types = types;
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
    public Collection<URI> getTypes() {
        return types;
    }
}
