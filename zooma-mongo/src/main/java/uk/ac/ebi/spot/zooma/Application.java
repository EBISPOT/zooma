package uk.ac.ebi.spot.zooma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by olgavrou on 03/08/2016.
 */
@SpringBootApplication
@ComponentScan("uk.ac.ebi.spot.zooma")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}