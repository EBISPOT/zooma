package uk.ac.ebi.spot.zooma.repository.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.messaging.converter.MessageConverter;
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

    private RabbitMessagingTemplate rabbitTemplate;

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public AnnotationRepositoryEventHandler(RabbitMessagingTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    @HandleAfterCreate
    public void handleAnnotationCreate(Annotation annotation) {
        getLog().info("New annotation created: " + annotation.getId());
        rabbitTemplate.convertAndSend(Constants.Queues.ANNOTATION_SAVE, annotation);
    }
}
