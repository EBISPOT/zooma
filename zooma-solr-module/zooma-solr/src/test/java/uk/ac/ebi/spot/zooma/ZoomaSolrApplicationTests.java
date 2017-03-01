package uk.ac.ebi.spot.zooma;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.zooma.config.SolrConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SolrConfig.class)
public class ZoomaSolrApplicationTests {
//
//	@Autowired
//	SolrAnnotationRepositoryService annotationRepositoryService;

	@Test
	public void contextLoads() {
//		List<AnnotationSummary> annotationList = annotationRepositoryService.getAnnotationSummariesByPropertyValueAndPropertyType("organism part", "liver");
	}

}
