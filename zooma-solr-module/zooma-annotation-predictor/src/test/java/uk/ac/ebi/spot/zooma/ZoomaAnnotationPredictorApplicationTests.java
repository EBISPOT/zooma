package uk.ac.ebi.spot.zooma;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.zooma.config.TestPredictorConfig;
import uk.ac.ebi.spot.zooma.model.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.service.AnnotationPredictionService;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestPredictorConfig.class)
public class ZoomaAnnotationPredictorApplicationTests {

	@Autowired
	AnnotationPredictionService annotationPredictionService;

	@Test
	public void predictAnnotationByPropertyValue() {
		ArrayList<AnnotationPrediction> predictions = annotationPredictionService.predict(null, "hereditary spastic paraplegia", null);
		for(AnnotationPrediction prediction : predictions){
//			assertTrue(prediction.getAnnotatedPropertyValue().contains("spastic"));
//			assertTrue(prediction.getAnnotatedPropertyValue().contains("paraplegia"));
			assertTrue(prediction.getConfidence().toString().toLowerCase().equals("good"));
		}

	}

}
