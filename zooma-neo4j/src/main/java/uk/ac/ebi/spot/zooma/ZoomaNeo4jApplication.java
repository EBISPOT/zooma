package uk.ac.ebi.spot.zooma;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.neo4j.*;
import uk.ac.ebi.spot.zooma.service.neo4j.AnnotationService;

import java.io.IOException;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
public class ZoomaNeo4jApplication {

	Logger log = Logger.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication.run(ZoomaNeo4jApplication.class, args);
	}


//	@Bean
//	CommandLineRunner init(RestTemplate restTemplate, AnnotationService annotationService) {
//
//		List<Annotation> annotations = new ArrayList<>();
//
//		ResponseEntity<String> allmongoentries = restTemplate.getForEntity("http://localhost:8080/annotations", String.class);
//
//		log.debug("Retrieving annotations from mongodb");
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
//					JsonNode bes = n.get("biologicalEntities");
//					String bioEntity = bes.get("bioEntity").asText();
//
//					JsonNode studies = bes.get("studies");
//					String study = studies.get("study").asText();
//
//					JsonNode prop = n.get("property");
//					String protype = prop.get("propertyType").asText();
//					String propvalue = prop.get("propertyValue").asText();
//
//					JsonNode prov = n.get("provenance");
//
//					JsonNode source = prov.get("source");
//					String name = source.get("name").asText();
//					String topic = source.get("topic").asText();
//					String type = source.get("type").asText();
//					String uri = source.get("uri").asText();
//
//					float quality = (float) n.get("quality").asDouble();
//					ArrayNode semtags = (ArrayNode) n.get("semanticTag");
//
//					Collection<SemanticTag> semanticTags = new ArrayList<>();
//					for (JsonNode s : semtags) {
//						SemanticTag semanticTag = new SemanticTag();
//						semanticTag.setSemanticTag(s.asText());
//						semanticTags.add(semanticTag);
//					}
//
//					Study stdy = new Study();
//					stdy.setStudy(study);
//					BiologicalEntity biologicalEntity = new BiologicalEntity();
//					biologicalEntity.setBioEntity(bioEntity);
//					biologicalEntity.setStudies(stdy);
//
//					Property property = new Property();
//					property.setPropertyType(protype);
//					property.setPropertyValue(propvalue);
//					property.setBiologicalEntity(biologicalEntity);
//
//					Source db = new Source();
//					db.setName(name);
//					db.setTopic(topic);
//					db.setType(type);
//					db.setUri(uri);
//
//					String evidence = prov.get("evidence").asText();
//					String accuracy = prov.get("accuracy").asText();
//					String annotator = prov.get("annotator").asText();
//					String generator = prov.get("generator").asText();
//					String generatedDate = prov.get("generatedDate").asText();
//					String annotatedDate = prov.get("annotationDate").asText();
//
//					AnnotationProvenance provenance = new AnnotationProvenance();
//					provenance.setAccuracy(accuracy);
//					provenance.setEvidence(evidence);
//					provenance.setAnnotator(annotator);
//					provenance.setAnnotationDate(annotatedDate);
//					provenance.setGeneratedDate(generatedDate);
//					provenance.setGenerator(generator);
//					provenance.setSource(db);
//
//					Annotation annotation = new Annotation();
//					annotation.setBiologicalEntities(biologicalEntity);
//					annotation.setProperty(property);
//					annotation.setSemanticTag(semanticTags);
//					annotation.setProvenance(provenance);
//					annotation.setBatchLoad(false);
//					annotation.setQuality(quality);
//
//					annotations.add(annotation);
//
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
//			int numDocuments = annotations.size();
//			log.debug("Extracted " + numDocuments + " documents");
//			// Save documents in batches
//        	int count = 0;
//        	while (count < numDocuments) {
//            	int end = count + 2000;
//            	if (end > numDocuments) {
//            	    end = numDocuments;
//            	}
//            	annotationService.save(annotations.subList(count, end));
//            	count = end;
//				log.debug("Saved " + count + " / " + numDocuments +  "  entries");
//        	}
//
//			log.info("Loading into neo4j finished...");
//	        log.info("Loaded: " + count + " annotations into neo4j");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}


}
