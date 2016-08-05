package uk.ac.ebi.spot.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.Collection;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Document(collection = "studies")
public class SimpleStudy extends SimpleDocument implements Study {

    private String accession;
    private Collection<URI> types;

    public SimpleStudy(String accession, Collection<URI> types) {
        this.accession = accession;
        this.types = types;
    }

    @Override public String getAccession() {
        return accession;
    }

    @Override
    public Collection<URI> getTypes() {
        return types;
    }

    @Override
    public URI getURI() {
        return null;
    }

    @Override public String toString() {
        return "SimpleStudy {\n" +
                "  uri='" + getURI() + "'\n" +
                "  accession='" + accession + "'\n}";
    }

}
