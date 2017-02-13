package uk.ac.ebi.spot.zooma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot.zooma")
public class ZoomaLoadAnnotationsApplication{
	public static void main(String[] args){
		SpringApplication.run(ZoomaLoadAnnotationsApplication.class, args);
	}
}
