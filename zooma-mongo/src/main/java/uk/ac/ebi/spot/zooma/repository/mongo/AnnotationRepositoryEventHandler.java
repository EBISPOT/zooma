package uk.ac.ebi.spot.zooma.repository.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.messaging.Constants;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/01/17
 */
@Service
@RepositoryEventHandler
public class AnnotationRepositoryEventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AmqpTemplate messagingTemplate;

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public AnnotationRepositoryEventHandler(AmqpTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @HandleAfterCreate
    public void handleAnnotationCreate(Annotation annotation) {
        getLog().info("New annotation created: " + annotation.getId());

        messagingTemplate.convertAndSend(Constants.Exchanges.ANNOTATION_FANOUT,"", annotation.toSimpleMap());
    }
}
