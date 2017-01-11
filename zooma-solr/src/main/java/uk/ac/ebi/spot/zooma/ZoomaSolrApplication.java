package uk.ac.ebi.spot.zooma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.zooma.service.SolrAnnotationRepositoryService;


@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaSolrApplication {

	@Autowired
	SolrAnnotationRepositoryService annotationSummaryRepositoryService;

	public static void main(String[] args) {
		SpringApplication.run(ZoomaSolrApplication.class, args);
	}

	@Bean
	CommandLineRunner run() {
//		Annotation solrAnnotation = new Annotation();
//		Property property = new MongoTypedProperty("New Type", "New Value");
//		solrAnnotation.setAnnotatedProperty(property);
//		Collection<URI> st = new ArrayList<>();
//		st.add(URI.create("AURI"));
//		solrAnnotation.setSemanticTags(st);
//		annotationSummaryRepositoryService.save(solrAnnotation);
//		solrAnnotation.set
////		List<AnnotationSummary> summaries = annotationSummaryRepositoryService.getByAnnotatedPropertyValue("Property value");
////		AnnotationSummary s = summaries.get(0);
////		annotationSummaryRepositoryService.delete(s);
//		annotationSummaryRepositoryService.save(solrAnnotationSummary);
//		List<AnnotationSummary> summaries = annotationSummaryRepositoryService.getByAnnotatedPropertyValue("Property value");
//		AnnotationSummary s = summaries.get(0);
//		annotationSummaryRepositoryService.save(s);
//		assert annotationSummaryRepositoryService.getAllDocuments().size() == 1;
		return null;
	}

}
