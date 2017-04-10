package uk.ac.ebi.spot.zooma;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.zooma.config.SolrConfigTest;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SolrConfigTest.class)
public class ZoomaSolrApplicationTests {

	@Autowired
	AnnotationRepositoryService summaryRepositoryService;

	@Test
	@Ignore
	public void contextLoads() throws IOException, SolrServerException {
//		List<Annotation> annotationList = summaryRepositoryService.findByPropertyTypeAndValue(new ArrayList<>(),"organism part", "liver");
//		assertTrue(annotationList.size() > 0);
		String value = "gastrocnemius and soleus muscle";
		String type = "organism part";
		ArrayList<String> source = new ArrayList<>();
//		source.add("eva-clinvar");
		List<Annotation> annotationList = summaryRepositoryService.findByPropertyTypeAndValue(source, type, value);
		if(annotationList.isEmpty()){
			annotationList = summaryRepositoryService.findByPropertyTypeAndValue(source,null, value);
		}
		for (Annotation summary : annotationList){
			System.out.println("propertyType: " + summary.getPropertyType());
			System.out.println("propertyType: " + summary.getPropertyValue());
			System.out.println("semanticTag: " + summary.getSemanticTag());
			System.out.println("score: " + summary.getQuality());
			System.out.println("source: " + summary.getSource());
			System.out.println("========");
		}
		assertTrue(annotationList.size() > 0);
	}

}
