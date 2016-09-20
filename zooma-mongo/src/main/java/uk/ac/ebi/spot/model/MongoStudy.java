package uk.ac.ebi.spot.model;

import java.net.URI;

/**
 * Created by olgavrou on 05/08/2016.
 */
public class MongoStudy implements Study {

    private String accession;
    private URI uri;

    public MongoStudy(String accession, URI uri) {

        this.accession = accession;
        this.uri = uri;
    }

    @Override public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
