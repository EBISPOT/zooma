package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of a Study designed to be used by jackson to deserialize annotation requests.  You should NOT use
 * this implementation in code; objects are designed to be transient and in order to handle serialization demands are
 * also mutable.  If you want to code with studies, using {@link uk.ac.ebi.fgpt.zooma.model.SimpleStudy} is advisable.
 *
 * @author Tony Burdett
 * @date 15/07/13
 */
public class StudyRequest implements Study {
    private static final long serialVersionUID = 8297260247536101633L;

    private String accession;
    private Collection<URI> types = Collections.emptySet();
    private URI uri;

    public void setURI(URI uri) {
        this.uri = uri;
    }

    @Override public URI getURI() {
        return uri;
    }

    @Override public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    @Override public String toString() {
        return "StudyRequest {\n" +
                "  uri='" + getURI() + "'\n" +
                "  accession='" + accession + "'\n}";
    }

    @Override
    public Collection<URI> getTypes() {
        return types;
    }

    public void setTypes (Collection<URI> types)  {
        this.types = types;
    }
}
