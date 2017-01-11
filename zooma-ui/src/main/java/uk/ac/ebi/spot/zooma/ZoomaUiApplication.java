package uk.ac.ebi.spot.zooma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoomaUiApplication.class, args);
	}
}
