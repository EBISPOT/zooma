package uk.ac.ebi.spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.service.SearchSolr;

import java.util.List;

@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class Mongo2solrApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Mongo2solrApplication.class, args);

		SearchSolr searchSolr = (SearchSolr) ctx.getBean("searchSolr");
		List<SolrAnnotation> solrAnnotations = searchSolr.findByAnnotatedPropertyValue("nifedipine 0.025 micromolar");
		System.out.println(solrAnnotations.size());
		MongoAnnotation mongoAnnotation = searchSolr.getMongoAnnotationById(solrAnnotations.get(0).getMongoid());
		System.out.println(mongoAnnotation.getSemanticTags());

//		Mongo2SolrLoader mongo2SolrLoader = (Mongo2SolrLoader) ctx.getBean("mongo2SolrLoader");
//		try {
//			mongo2SolrLoader.load();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}


}
