package uk.ac.ebi.spot.zooma.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.Annotation;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@Component
public class AnnotationSubmissionReceiver {
    @RabbitListener(queues = "annotation.save.queue")
    public void handleAnnotationSubmission(Annotation annotation) {
        System.out.println("We have detected that a new message was received! " + annotation);
    }
}
