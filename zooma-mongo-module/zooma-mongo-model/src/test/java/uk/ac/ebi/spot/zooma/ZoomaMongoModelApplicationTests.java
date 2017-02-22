package uk.ac.ebi.spot.zooma;

import org.junit.Test;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ZoomaMongoModelApplicationTests {

	@Test(expected = IllegalArgumentException.class)
	public void testAnnotationQualityIsSetOnCreation() {
		BaseAnnotation annotation = new BaseAnnotation();
		DatabaseAnnotationSource source = new DatabaseAnnotationSource("www.ebi.ac.uk/sysmicro", "atlas", "Phenotypes");

		DateTimeFormatter dashedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime;
		String annotationdate = "2017-02-08 16:52:08";
		dateTime = LocalDateTime.parse(annotationdate, dashedDateFormatter);

		MongoAnnotationProvenance provenance = new MongoAnnotationProvenance(source, AnnotationProvenance.Evidence.MANUAL_CURATED,
				AnnotationProvenance.Accuracy.PRECISE,
				"ZOOMA",
				"Annotator name",
				dateTime);


		assertNull(annotation.getQuality());
		annotation.setProvenance(provenance);
		assertNotNull(annotation.getQuality());
	}

	@Test
	public void testMongoAnnotationProvenanceGeneratedDateIsSetOnCreation(){
		DatabaseAnnotationSource source = new DatabaseAnnotationSource("www.ebi.ac.uk/sysmicro", "atlas", "Phenotypes");
		MongoAnnotationProvenance provenance = new MongoAnnotationProvenance(source, AnnotationProvenance.Evidence.MANUAL_CURATED,
				AnnotationProvenance.Accuracy.PRECISE,
				"ZOOMA",
				"Annotator name",
				LocalDateTime.now());
		assertNotNull(provenance.getGeneratedDate());
	}

}
