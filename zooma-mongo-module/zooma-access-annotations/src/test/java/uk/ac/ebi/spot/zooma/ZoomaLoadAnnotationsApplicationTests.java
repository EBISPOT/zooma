package uk.ac.ebi.spot.zooma;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.controller.AnnotationController;
import uk.ac.ebi.spot.zooma.model.*;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ZoomaLoadAnnotationsApplicationTests {

	private MockMvc mockMvc;


	private MediaType contentType = MediaType.APPLICATION_JSON;

	private MockRestServiceServer mockServer;

	private BaseAnnotation annotation = new BaseAnnotation();

	@Before
	public void setup() throws IOException {

		final AnnotationController controller = new AnnotationController();
		final RestTemplate restTemplate = new RestTemplate();
		this.mockServer = MockRestServiceServer.createServer(restTemplate);

		ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);

		setAnnotationTemplate();

		this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void createMongoAnnotation() throws Exception {
		setPostResponse();
		mockMvc.perform(post("/annotations").contentType(contentType).content(this.annotation.toString()))
				.andExpect(status().isCreated());
	}

	@Test
	public void getMongoAnnotations() throws Exception{
		setGetResponse();
		mockMvc.perform(get("/annotations").contentType(contentType))
				.andExpect(status().isOk());
	}

	@Test
	public void getMongoAnnotation() throws Exception{
		setGetAnnotationResponse();
		mockMvc.perform(get("/annotations/589dd52fc4d0b0529f123dee").contentType(contentType))
				.andExpect(status().isOk());
	}

	private void setGetAnnotationResponse() {
		this.mockServer.expect(requestTo("http://localhost:8080/annotations/589dd52fc4d0b0529f123dee"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\n" +
						"\"biologicalEntities\": {\n" +
						"\"bioEntity\": \"GSE10927GSM277147\",\n" +
						"\"studies\": {\n" +
						"\"study\": \"E-GEOD-10927\",\n" +
						"\"studyUri\": null\n" +
						"},\n" +
						"\"bioEntityUri\": null\n" +
						"},\n" +
						"\"property\": {\n" +
						"\"propertyType\": \"biopsy site\",\n" +
						"\"propertyValue\": \"left\"\n" +
						"},\n" +
						"\"semanticTag\": [\n" +
						"  \"http://www.ebi.ac.uk/efo/EFO_0001658\"\n" +
						"],\n" +
						"\"provenance\": {\n" +
						"\"source\": {\n" +
						"\"uri\": \"uri\",\n" +
						"\"name\": \"name\",\n" +
						"\"topic\": \"topic\",\n" +
						"\"type\": \"DATABASE\"\n" +
						"},\n" +
						"\"evidence\": \"MANUAL_CURATED\",\n" +
						"\"accuracy\": \"PRECISE\",\n" +
						"\"generator\": \"ZOOMA\",\n" +
						"\"annotator\": \"Eleanor Williams\",\n" +
						"\"annotationDate\": \"2014-10-01T18:32:00\",\n" +
						"\"generatedDate\": \"2017-02-14T11:17:02.903\"\n" +
						"},\n" +
						"\"batchLoad\": false,\n" +
						"\"quality\": 21.824257\n" +
						"}", MediaType.APPLICATION_JSON));

	}


	private void setPostResponse() throws IOException{
		HttpHeaders mockHeader = new HttpHeaders();
		mockHeader.setLocation(URI.create("URI"));
		this.mockServer.expect(requestTo("http://localhost:8080/annotations"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess().headers(mockHeader));
	}

	private void setGetResponse() throws IOException {
		String s = "{\"_embedded\": {\n" +
				"\"annotations\": [\n" +
				annotation.toString() +
				"]},\n" +
				"\"_links\": {\n" +
				"\"first\": {\n" +
				"\"href\": \"http://localhost:8080/annotations?page=0&size=20\"\n" +
				"},\n" +
				"\"self\": {\n" +
				"\"href\": \"http://localhost:8080/annotations\"\n" +
				"},\n" +
				"\"next\": {\n" +
				"\"href\": \"http://localhost:8080/annotations?page=1&size=20\"\n" +
				"},\n" +
				"\"last\": {\n" +
				"\"href\": \"http://localhost:8080/annotations?page=104&size=20\"\n" +
				"},\n" +
				"\"profile\": {\n" +
				"\"href\": \"http://localhost:8080/profile/annotations\"\n" +
				"},\n" +
				"\"search\": {\n" +
				"\"href\": \"http://localhost:8080/annotations/search\"\n" +
				"}\n" +
				"},\n" +
				"\"page\": {\n" +
				"\"size\": 20,\n" +
				"\"totalElements\": 2082,\n" +
				"\"totalPages\": 105,\n" +
				"\"number\": 0\n" +
				"}}";
		this.mockServer.expect(requestTo("http://localhost:8080/annotations"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(s, MediaType.APPLICATION_JSON));
	}

	private void setAnnotationTemplate(){
		BiologicalEntity be = new BiologicalEntity("GSE10927GSM277147", new Study("E-GEOD-10927"));
		DatabaseAnnotationSource source = new DatabaseAnnotationSource("www.ebi.ac.uk/sysmicro", "atlas", "Phenotypes");

		MongoAnnotationProvenance provenance = new MongoAnnotationProvenance(source, AnnotationProvenance.Evidence.MANUAL_CURATED,
				AnnotationProvenance.Accuracy.PRECISE,
				"generator",
				"annotator",
				LocalDateTime.now());

		this.annotation.setBiologicalEntities(be);
		this.annotation.setProvenance(provenance);
		Collection st = new ArrayList();
		st.add("http://www.ebi.ac.uk/efo/EFO_0001658");
		this.annotation.setSemanticTag(st);
		TypedProperty property = new TypedProperty("biopsy site", "left");
		this.annotation.setProperty(property);
		this.annotation.setBatchLoad(false);
	}

}
