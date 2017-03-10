package uk.ac.ebi.spot.zooma;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableRabbit
@ComponentScan("uk.ac.ebi.spot.zooma")
/**
 * Zooma Neo4j Application is the Neo endpoint of Zooma.
 * It has a RabbitMQ listener {@link uk.ac.ebi.spot.zooma.messaging.neo4j.AnnotationSumbissionReceiver}
 * that listens to a queue and if there is a new Annotation added to Zooma it will merge it to the graph.
 */
public class ZoomaNeo4jApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ZoomaNeo4jApplication.class, args);
	}
}
