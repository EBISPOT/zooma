package uk.ac.ebi.spot.zooma.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.Annotation;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@Service
public class AnnotationSubmissionReceiver {
    @RabbitListener(queues = "annotation.save.queue")
    public void handleAnnotationSubmission(Annotation annotation) {
        System.out.println("We have detected that a new annotation was submitted! " + annotation);
    }
}
