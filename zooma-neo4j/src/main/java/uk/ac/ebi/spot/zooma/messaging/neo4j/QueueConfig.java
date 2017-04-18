package uk.ac.ebi.spot.zooma.messaging.neo4j;

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
    @Bean Queue queueNeo() { return new Queue(Constants.Queues.ANNOTATION_SAVE_NEO, false); }
    @Bean Queue queueNeoReplace() { return new Queue(Constants.Queues.ANNOTATION_REPLACE_NEO, false); }
    @Bean FanoutExchange exchange() { return new FanoutExchange(Constants.Exchanges.ANNOTATION_FANOUT); }
    @Bean FanoutExchange exchangeReplace() { return new FanoutExchange(Constants.Exchanges.ANNOTATION_FANOUT_REPLACEMENT); }
    @Bean Binding bindingNeo(Queue queueNeo, FanoutExchange exchange) { return BindingBuilder.bind(queueNeo).to(exchange); }
    @Bean Binding bindingNeoUpdate(Queue queueNeoReplace, FanoutExchange exchangeReplace) { return BindingBuilder.bind(queueNeoReplace).to(exchangeReplace); }
}
