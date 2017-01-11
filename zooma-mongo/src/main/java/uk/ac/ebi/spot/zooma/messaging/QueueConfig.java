package uk.ac.ebi.spot.zooma.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/01/17
 */
@Configuration
public class QueueConfig {
    @Bean Queue queue() { return new Queue(Constants.Queues.ANNOTATION_SAVE, false); }
    @Bean Binding binding(Queue queue, FanoutExchange exchange) { return BindingBuilder.bind(queue).to(exchange); }
    @Bean FanoutExchange exchange() { return new FanoutExchange(Constants.Exchanges.ANNOTATION_FANOUT); }
}
