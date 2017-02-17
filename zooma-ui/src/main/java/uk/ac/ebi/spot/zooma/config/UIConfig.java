package uk.ac.ebi.spot.zooma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olgavrou on 04/11/2016.
 */
@Configuration
public class UIConfig {

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(25);
        threadPoolTaskExecutor.setMaxPoolSize(25);
        threadPoolTaskExecutor.setQueueCapacity(25);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);

        return threadPoolTaskExecutor;
    }
}
