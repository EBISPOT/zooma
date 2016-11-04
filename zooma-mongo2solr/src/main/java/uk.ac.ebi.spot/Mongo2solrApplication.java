package uk.ac.ebi.spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.model.SolrAnnotationSummary;
import uk.ac.ebi.spot.service.SearchSolr;

import java.util.List;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class Mongo2solrApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Mongo2solrApplication.class, args);

		SearchSolr searchSolr = (SearchSolr) ctx.getBean("searchSolr");
		List<AnnotationSummary> solrAnnotations = searchSolr.findByAnnotatedPropertyValue("CD4-positive");
		System.out.println(solrAnnotations.size());
		if (solrAnnotations!= null && !solrAnnotations.isEmpty()) {
			SolrAnnotationSummary solrAnnotationSummary = (SolrAnnotationSummary) solrAnnotations.get(0);
			MongoAnnotation mongoAnnotation = searchSolr.getMongoAnnotationById(solrAnnotationSummary.getMongoid());
			System.out.println(mongoAnnotation.getSemanticTags());
		}

	}

}