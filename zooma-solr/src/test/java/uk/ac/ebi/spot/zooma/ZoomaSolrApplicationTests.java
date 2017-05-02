package uk.ac.ebi.spot.zooma;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.zooma.config.SolrConfigTest;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryServiceRead;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SolrConfigTest.class)
public class ZoomaSolrApplicationTests {

	@Autowired
	AnnotationRepositoryServiceRead summaryRepositoryService;

	@Test
	@Ignore
	public void contextLoads() throws IOException, SolrServerException {
//		List<Annotation> annotationList = summaryRepositoryService.findByPropertyTypeAndValue(new ArrayList<>(),"organism part", "liver");
//		assertTrue(annotationList.size() > 0);
		String value = "cell migration";
		String type = "phenotype";
		Page<Annotation> annotationList = summaryRepositoryService.findByPropertyTypeAndPropertyValue(type, value, new PageRequest(0, 20));
		if(annotationList.getContent().isEmpty()){
			annotationList = summaryRepositoryService.findByPropertyValue(value, new PageRequest(0, 20));
		}
		for (Annotation summary : annotationList){
			System.out.println("propertyType: " + summary.getPropertyType());
			System.out.println("propertyType: " + summary.getPropertyValue());
			System.out.println("semanticTag: " + summary.getSemanticTag());
			System.out.println("score: " + summary.getQuality());
			System.out.println("source: " + summary.getSource());
			System.out.println("========");
		}
		assertTrue(annotationList.getTotalElements() > 0);
	}

}
