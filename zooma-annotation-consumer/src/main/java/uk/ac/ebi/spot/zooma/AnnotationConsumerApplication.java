package uk.ac.ebi.spot.zooma;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@SpringBootApplication
@EnableRabbit
public class AnnotationConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnnotationConsumerApplication.class, args);
    }
}
