package uk.ac.ebi.spot.zooma;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.InputStreamResource;
import uk.ac.ebi.spot.zooma.service.AnnotationItemProcessor;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;
import uk.ac.ebi.spot.zooma.service.CustomItemWriter;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by olgavrou on 08/02/2017.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableBatchProcessing
@ComponentScan("uk.ac.ebi.spot.zooma")
public class ZoomaCSVLoaderApplication {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("${delimeter}")
    private String delimeter;

    @Value("${loadFrom}")
    private String loadFrom;


    @Bean
    public FlatFileItemReader<SimpleAnnotation> reader(){
        FlatFileItemReader<SimpleAnnotation> reader = new FlatFileItemReader<>();
        try {
//            URL url = new URL("ftp://ftp.ebi.ac.uk/pub/databases/spot/zooma/data/annotations/cbi/latest/biosample_plant.csv");
            URL url = new URL(loadFrom);
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            URLConnection urlConnection2 = url.openConnection();
            InputStream in2 = urlConnection2.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String firstLine = bufferedReader.readLine();
            bufferedReader.close();
            firstLine = firstLine.replace("_", "");
            String[] headers = firstLine.split(delimeter);


            reader.setResource(new InputStreamResource(in2));
            reader.setLineMapper(new DefaultLineMapper<SimpleAnnotation>() {{
                setLineTokenizer(new DelimitedLineTokenizer(){{
                    setDelimiter(delimeter);
                    setNames(new String[] {headers[0].toLowerCase(),
                            headers[1].toLowerCase(),
                            headers[2].toLowerCase(),
                            headers[3].toLowerCase(),
                            headers[4].toLowerCase(),
                            headers[5].toLowerCase(),
                            headers[6].toLowerCase()});
                }});
                setFieldSetMapper(new BeanWrapperFieldSetMapper<SimpleAnnotation>(){{
                    setTargetType(SimpleAnnotation.class);
                }});
            }});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    @Bean
    public AnnotationItemProcessor processor(){
        return new AnnotationItemProcessor();
    }

    @Bean
    public CustomItemWriter writer(){
        CustomItemWriter writer = new CustomItemWriter();
        return writer;
    }


    @Bean
    public Job importUserJob(){
        return jobBuilderFactory.get("importUserJob")
                .start(step1())
                .build();
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .<SimpleAnnotation, SimpleAnnotation> chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    public static void main(String[] args){
        System.exit(SpringApplication.exit(SpringApplication.run(ZoomaCSVLoaderApplication.class, args)));
    }
}
