package uk.ac.ebi.spot.zooma;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.neo4j.*;
import uk.ac.ebi.spot.zooma.service.neo4j.AnnotationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot.zooma")
public class ZoomaNeo4jApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoomaNeo4jApplication.class, args);
	}
}
