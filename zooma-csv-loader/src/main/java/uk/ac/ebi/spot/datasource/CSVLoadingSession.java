package uk.ac.ebi.spot.datasource;

import uk.ac.ebi.spot.builders.AnnotationProvenanceBuilder;
import uk.ac.ebi.spot.model.AnnotationProvenance;
import uk.ac.ebi.spot.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
import java.util.Date;

/**
 * An annotation loading session that is capable of minting generate URIs specific to some data contained in a CSV file
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVLoadingSession extends AbstractAnnotationLoadingSession {

    private String name;
    private URI uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     * and creates the AnnotationProvenanceTemplate
     */
    @Override
    public void init() {
        super.init();
        setAnnotationProvenanceTemplate(
                AnnotationProvenanceBuilder
                        .createTemplate(uri.toString(), new Date())
                        .sourceIs(new SimpleDatabaseAnnotationSource(uri, name))
                        .evidenceIs(AnnotationProvenance.Evidence.MANUAL_CURATED)
                        .accuracyIs(AnnotationProvenance.Accuracy.NOT_SPECIFIED));
    }
}
