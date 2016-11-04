package uk.ac.ebi.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;
import uk.ac.ebi.spot.services.LoadAndSaveService;


@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaLoadCsvToMongo {

	@Autowired
	MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ZoomaLoadCsvToMongo.class, args);

		LoadAndSaveService threadBuilder = (LoadAndSaveService) ctx.getBean("loadAndSaveService");
		threadBuilder.init();

		ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) ctx.getBean("taskExecutor");
		for (;;){
			int count = threadPoolTaskExecutor.getActiveCount();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (count == 0){
				threadPoolTaskExecutor.shutdown();
				break;
			}
		}
		ctx.close();
	}

}