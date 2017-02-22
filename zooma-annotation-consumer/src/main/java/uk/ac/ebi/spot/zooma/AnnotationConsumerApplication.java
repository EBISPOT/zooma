package uk.ac.ebi.spot.zooma;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@SpringBootApplication
@EnableRabbit
public class AnnotationConsumerApplication {
    @Bean MessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    public static void main(String[] args) {
        SpringApplication.run(AnnotationConsumerApplication.class, args);
    }
}
