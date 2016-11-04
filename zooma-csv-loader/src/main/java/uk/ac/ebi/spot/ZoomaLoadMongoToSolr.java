package uk.ac.ebi.spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.services.Mongo2SolrLoader;

import java.io.IOException;

/**
 * Created by olgavrou on 28/10/2016.
 */
@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaLoadMongoToSolr {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ZoomaLoadMongoToSolr.class, args);

		Mongo2SolrLoader mongo2SolrLoader = (Mongo2SolrLoader) ctx.getBean("mongo2SolrLoader");
		try {
			mongo2SolrLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

}
