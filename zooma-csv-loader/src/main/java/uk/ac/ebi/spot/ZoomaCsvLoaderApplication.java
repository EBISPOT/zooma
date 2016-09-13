package uk.ac.ebi.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.service.CSVLoader;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.model.SimpleAnnotation;
import uk.ac.ebi.spot.services.AnnotationRepositoryService;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class ZoomaCsvLoaderApplication {


	@Autowired
	private Collection<CSVLoader> csvLoaders;

	@Autowired
	AnnotationRepositoryService annotationRepositoryService;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ZoomaCsvLoaderApplication.class, args);
	}

	/*
		Testing the csvLoaders
	 */
	@Bean
	CommandLineRunner run() {
		for (CSVLoader loader : csvLoaders){
			try {
				List<Annotation> annotationList = loader.load();
				for (Annotation annotation : annotationList) {
					SimpleAnnotation simpleAnnotation = (SimpleAnnotation) annotation;
					annotationRepositoryService.save(simpleAnnotation);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}