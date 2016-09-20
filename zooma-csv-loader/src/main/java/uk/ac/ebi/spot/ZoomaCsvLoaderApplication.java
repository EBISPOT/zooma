package uk.ac.ebi.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;
import uk.ac.ebi.spot.builders.LoaderThreadBuilder;


@SpringBootApplication
public class ZoomaCsvLoaderApplication {

	@Autowired
	MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ZoomaCsvLoaderApplication.class, args);

		LoaderThreadBuilder threadBuilder = (LoaderThreadBuilder) ctx.getBean("loaderThreadBuilder");
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