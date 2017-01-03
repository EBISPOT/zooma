package uk.ac.ebi.spot.services;

import uk.ac.ebi.spot.config.AnnotationProvenanceBuilder;
import uk.ac.ebi.spot.model.AnnotationProvenance;
import uk.ac.ebi.spot.model.MongoDatabaseAnnotationSource;

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
    private String uri;
    private String topic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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
                        .sourceIs(new MongoDatabaseAnnotationSource(uri, name, topic))
                        .evidenceIs(AnnotationProvenance.Evidence.MANUAL_CURATED)
                        .accuracyIs(AnnotationProvenance.Accuracy.NOT_SPECIFIED));
    }
}
