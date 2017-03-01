package uk.ac.ebi.spot.zooma.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.SolrAnnotationRepository;



/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@Component
public class SolrAnnotationSubmissionReceiver {

    @Autowired
    SolrAnnotationRepository solrAnnotationRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @RabbitListener(queues = "annotation.save.solr.queue")
    public void handleAnnotationSubmission(Annotation annotation) {

        getLog().info("Solr Queue: We have detected that a new message was received! " + annotation.getId());

        uk.ac.ebi.spot.zooma.model.solr.Annotation solrAnn = new uk.ac.ebi.spot.zooma.model.solr.Annotation();
        solrAnn.setMongoid(annotation.getId());
        solrAnn.setQuality(annotation.getQuality());
        solrAnn.setPropertyType(annotation.getProperty().getPropertyType());
        solrAnn.setPropertyValue(annotation.getProperty().getPropertyValue());
        solrAnn.setSemanticTag(annotation.getSemanticTag());
        solrAnn.setSource(annotation.getProvenance().getSource().getName());

        solrAnnotationRepository.save(solrAnn);
        getLog().info("Solr Queue: We have saved the annotation into Solr! " + annotation.getId());
    }
}
