package uk.ac.ebi.spot.zooma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.SolrAnnotationRepository;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ZoomaSolrTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoomaSolrTemplateApplication.class, args);
	}

}
