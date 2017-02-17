package uk.ac.ebi.spot.zooma;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.SolrAnnotationRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaSolrApplication {
	public static void main(String[] args) {
		SpringApplication.run(ZoomaSolrApplication.class, args);
	}

	@Bean
	CommandLineRunner init(SolrAnnotationRepository repository, RestTemplate restTemplate) {

		Collection<String> st = new ArrayList<>();
		st.add("http://purl.obolibrary.org/obo/NCBITaxon_10091");
		st.add("http://efo_001");
		String source = "atlas";

		Annotation annotation = new Annotation("something", "Mus musculus", st, "mongoid", source, 20.1f);
		repository.save(annotation);

		st = new ArrayList<>();
		st.add("http://purl.obolibrary.org/obo/NCBITaxon_10090");
		annotation = new Annotation("organism", "Mus musculus", st, "mongoid", source, 20.1f);
		repository.save(annotation);

		st = new ArrayList<>();
		st.add("http://purl.obolibrary.org/obo/NCBITaxon_10080");
		annotation = new Annotation("organism", "Mus musculus mu", st, "mongoid", source, 20.1f);
		repository.save(annotation);
		return null;
	}
}
