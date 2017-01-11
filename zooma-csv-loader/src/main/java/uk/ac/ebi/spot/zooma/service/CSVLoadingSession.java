package uk.ac.ebi.spot.zooma.service;

import uk.ac.ebi.spot.zooma.config.AnnotationProvenanceBuilder;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;
import uk.ac.ebi.spot.zooma.model.mongo.DatabaseAnnotationSource;

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
                        .sourceIs(new DatabaseAnnotationSource(uri, name, topic))
                        .evidenceIs(AnnotationProvenance.Evidence.MANUAL_CURATED)
                        .accuracyIs(AnnotationProvenance.Accuracy.NOT_SPECIFIED));
    }
}
