package uk.ac.ebi.spot.zooma.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 14/02/17
 */
@Component
@Configuration
public class ReceiverConfigurer implements RabbitListenerConfigurer {
    @Bean DefaultMessageHandlerMethodFactory handlerMethodFactory() { return new DefaultMessageHandlerMethodFactory(); }

    @Override public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(handlerMethodFactory());
    }
}
