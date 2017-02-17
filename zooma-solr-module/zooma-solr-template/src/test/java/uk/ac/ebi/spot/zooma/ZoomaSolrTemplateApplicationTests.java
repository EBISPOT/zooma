package uk.ac.ebi.spot.zooma;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.zooma.config.TemplateTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TemplateTestConfig.class)
public class ZoomaSolrTemplateApplicationTests {

	@Test
	public void contextLoads() {
	}

}
