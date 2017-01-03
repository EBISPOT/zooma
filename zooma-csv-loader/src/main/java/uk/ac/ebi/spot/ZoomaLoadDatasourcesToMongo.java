package uk.ac.ebi.spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.model.MongoDatabaseAnnotationSource;
import uk.ac.ebi.spot.services.CSVLoadingSession;
import uk.ac.ebi.spot.services.MongoAnnotationSourceRepositoryService;

import java.util.Map;

/**
 * Created by olgavrou on 24/11/2016.
 */
@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaLoadDatasourcesToMongo {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ZoomaLoadDatasourcesToMongo.class, args);

        //load datasources to mongo
		MongoAnnotationSourceRepositoryService mongoAnnotationSourceRepositoryService = (MongoAnnotationSourceRepositoryService) ctx.getBean("mongoAnnotationSourceRepositoryService");
		Map<String, CSVLoadingSession> loadingSessionMap = ctx.getBeansOfType(CSVLoadingSession.class);
        for (CSVLoadingSession loadingSession : loadingSessionMap.values()){
            String topic = loadingSession.getTopic();
            String name = loadingSession.getName();
            String uri = loadingSession.getUri();
            MongoDatabaseAnnotationSource annotationSource = new MongoDatabaseAnnotationSource(uri, name, topic);
            mongoAnnotationSourceRepositoryService.update(annotationSource);
        }

        ctx.close();
    }
}
