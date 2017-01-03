package uk.ac.ebi.spot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.config.SolrConfig;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.services.SolrAnnotationRepositoryService;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SolrConfig.class)
public class ZoomaSolrApplicationTests {

	@Autowired
	SolrAnnotationRepositoryService annotationRepositoryService;

	@Test
	public void contextLoads() {
		List<AnnotationSummary> annotationList = annotationRepositoryService.getAnnotationSummariesByPropertyValue("liver");
	}

}
