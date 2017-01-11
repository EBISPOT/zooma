package uk.ac.ebi.spot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.messaging.converter.MessageConverter;
import uk.ac.ebi.spot.model.MongoAnnotation;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/01/17
 */
@RepositoryEventHandler
public class AnnotationEventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private RabbitMessagingTemplate rabbitTemplate;

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public AnnotationEventHandler(RabbitMessagingTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    @HandleAfterSave
    public void handleAnnotationSave(MongoAnnotation annotation) {
        getLog().info("New annotation saved: " + annotation.getId());
        rabbitTemplate.convertAndSend(annotation);
    }
}
