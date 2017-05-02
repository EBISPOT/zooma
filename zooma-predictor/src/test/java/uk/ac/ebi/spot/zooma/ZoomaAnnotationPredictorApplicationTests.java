package uk.ac.ebi.spot.zooma;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.zooma.config.TestPredictorConfig;
import uk.ac.ebi.spot.zooma.service.AnnotationPredictionService;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestPredictorConfig.class)
public class ZoomaAnnotationPredictorApplicationTests {

	@Autowired
	AnnotationPredictionService annotationPredictionService;

	@Test
	@Ignore
	public void predictAnnotationByPropertyValue() throws URISyntaxException {
		annotationPredictionService.predictByPropertyValue("cell death");
//		for(AnnotationPrediction prediction : predictions){
////			assertTrue(prediction.getAnnotatedPropertyValue().contains("spastic"));
////			assertTrue(prediction.getAnnotatedPropertyValue().contains("paraplegia"));
//			assertTrue(prediction.getConfidence().toString().toLowerCase().equals("medium"));
//		}

	}

}
