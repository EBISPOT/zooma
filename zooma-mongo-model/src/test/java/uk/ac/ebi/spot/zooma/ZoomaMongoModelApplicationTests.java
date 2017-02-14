package uk.ac.ebi.spot.zooma;

import org.junit.Test;
import uk.ac.ebi.spot.zooma.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ZoomaMongoModelApplicationTests {

//	@Test(expected = IllegalArgumentException.class)
	@Test
	public void testAnnotationQualityIsSetOnCreation() {
		Annotation annotation = new Annotation();
		DatabaseAnnotationSource source = new DatabaseAnnotationSource("www.ebi.ac.uk/sysmicro", "atlas", "Phenotypes");

		DateTimeFormatter dashedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime;
		String annotationdate = "2017-02-08 16:52:08";
		dateTime = LocalDateTime.parse(annotationdate, dashedDateFormatter);

		AnnotationProvenance
                provenance = new AnnotationProvenance(source, uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                      uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance.Accuracy.PRECISE,
                                                      "ZOOMA",
                                                      "Annotator name",
                                                      dateTime);


		assertNull(annotation.getQuality());
		annotation.setProvenance(provenance);
//		assertNotNull(annotation.getQuality());
	}

	@Test
	public void testMongoAnnotationProvenanceGeneratedDateIsSetOnCreation(){
		DatabaseAnnotationSource source = new DatabaseAnnotationSource("www.ebi.ac.uk/sysmicro", "atlas", "Phenotypes");
		AnnotationProvenance
                provenance = new AnnotationProvenance(source, uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                      uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance.Accuracy.PRECISE,
                                                      "ZOOMA",
                                                      "Annotator name",
                                                      LocalDateTime.now());
		assertNotNull(provenance.getGeneratedDate());
	}

}
