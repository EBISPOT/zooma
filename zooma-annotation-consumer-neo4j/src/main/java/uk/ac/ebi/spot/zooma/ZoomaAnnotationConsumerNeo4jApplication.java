package uk.ac.ebi.spot.zooma;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableRabbit
@ComponentScan("uk.ac.ebi.spot.zooma")
public class ZoomaAnnotationConsumerNeo4jApplication {
	public static void main(String[] args) {
		SpringApplication.run(ZoomaAnnotationConsumerNeo4jApplication.class, args);
	}
}
