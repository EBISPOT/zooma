package uk.ac.ebi.spot.zooma;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.SolrAnnotationRepository;
import uk.ac.ebi.spot.zooma.service.AnnotationSummaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
public class ZoomaSolrTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoomaSolrTemplateApplication.class, args);
	}

	@Bean
	CommandLineRunner init(AnnotationSummaryService annotationsService, RestTemplate restTemplate, SolrAnnotationRepository annotationRepository) throws SolrServerException, IOException {

		List<Annotation> annotations = new ArrayList<>();

		List<String> st = new ArrayList<>();
		st.add("http://www.ebi.ac.uk/efo/EFO_00001");
		Annotation annotation = new Annotation("disease" , "liver carcinoma", st, "mongoid", "name", 21.0f);
		annotationRepository.save(annotation);
//		ResponseEntity<String> allmongoentries = restTemplate.getForEntity("http://localhost:8080/annotations", String.class);
//
//		String body = allmongoentries.getBody();
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			JsonNode node = mapper.readTree(body);
//			boolean last = false;
//			while (!last) {
//				ArrayNode arrayNode = (ArrayNode) node.get("_embedded").get("annotations");
//
//				Iterator iterator = arrayNode.iterator();
//				while (iterator.hasNext()) {
//					JsonNode n = (JsonNode) iterator.next();
//
//					JsonNode prop = n.get("property");
//					String protype = prop.get("propertyType").asText();
//					String propvalue = prop.get("propertyValue").asText();
//					JsonNode prov = n.get("provenance");
//					JsonNode source = prov.get("source");
//					String name = source.get("name").asText();
//					float quality = (float) n.get("quality").asDouble();
//					ArrayNode semtags = (ArrayNode) n.get("semanticTag");
//					Collection<String> st = new ArrayList<>();
//					for (JsonNode s : semtags) {
//						st.add(s.asText());
//					}
//
//					String sources = name;
//
//					annotations.add(new Annotation(protype, propvalue, st, "mongoid", sources, quality));
//				}
//				JsonNode next = node.get("_links").get("next");
//				if(next == null){
//					last = true;
//				} else {
//					String nextPage = next.get("href").asText();
//					allmongoentries = restTemplate.getForEntity(nextPage, String.class);
//					body = allmongoentries.getBody();
//					node = mapper.readTree(body);
//				}
//			}
//
//			int stored = 0;
//			for(Annotation annotation : annotations){
////				boolean updated = annotationsService.(annotation);
////				if (!updated) {
//				annotationRepository.save(annotation);
//					stored++;
////				} else {
////					upd++;
////				}
//			}
//
//			System.out.println("stored: " + stored);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		annotationsService.findAnnotationSummariesByPropertyValue("mus musculus");

		return null;
	}


}
