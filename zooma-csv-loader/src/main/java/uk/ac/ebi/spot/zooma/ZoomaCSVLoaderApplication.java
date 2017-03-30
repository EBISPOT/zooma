package uk.ac.ebi.spot.zooma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import uk.ac.ebi.spot.zooma.service.AnnotationHandler;
import uk.ac.ebi.spot.zooma.service.AnnotationItemProcessor;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;
import uk.ac.ebi.spot.zooma.service.CustomItemWriter;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

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

    @Autowired
    private AnnotationHandler annotationHandler;

    @Value("${delimeter}")
    private String delimeter;

    @Value("${loadFrom}")
    private String loadFrom;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }



    @Bean
    FlatFileItemWriter<SimpleAnnotation> flatFileItemWriter(){
        FlatFileItemWriter writer = null;
        try {
            String[] headers = new String[0];

            URL url = new URL(loadFrom);
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String firstLine = bufferedReader.readLine();
            in.close();
            bufferedReader.close();

            String headerForNewFile = calculateOutputHeader(firstLine, delimeter);

            firstLine = firstLine.replace("_", "");
            headers = firstLine.split(delimeter);

            String[] writeHeaders = calculateHeaders(headers, true);

            String[] split = loadFrom.split("/");
            String fileName = split[split.length - 1];
            String reportF = fileName.contains(".") ? fileName.split("\\.")[0] : fileName;

            DelimitedLineAggregator<SimpleAnnotation> aggregator = new DelimitedLineAggregator<>();
            aggregator.setDelimiter(delimeter);

            writer = new CustomItemWriter(this.annotationHandler, headerForNewFile);

            //TODO: temporary report generated location
            writer.setResource(new FileSystemResource("./" + reportF + "_" + LocalDateTime.now() + "_Report.txt"));
            BeanWrapperFieldExtractor<SimpleAnnotation> fieldExtractor = new BeanWrapperFieldExtractor<>();
            fieldExtractor.setNames(writeHeaders);
            aggregator.setFieldExtractor(fieldExtractor);
            writer.setLineAggregator(aggregator);
        } catch (IOException e) {
            getLog().error("Failed to read file: " + loadFrom + " " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return writer;
    }

    @Bean
    public FlatFileItemReader<SimpleAnnotation> reader(){
        FlatFileItemReader<SimpleAnnotation> reader = new FlatFileItemReader<>();
        try {
            URL url = new URL(loadFrom);
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String firstLine = bufferedReader.readLine();
            bufferedReader.close();

            validateHeaders(firstLine.split(delimeter));
            firstLine = firstLine.replace("_", "");
            String[] headers = firstLine.split(delimeter);

            String[] readHeaders = calculateHeaders(headers, false);

            int[] includedFieldPositions = calculateFieldPositions(headers, readHeaders.length);

            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(){{
                setDelimiter(delimeter);
                setNames(readHeaders);
                setIncludedFields(includedFieldPositions);
            }};

            reader.setResource(new UrlResource(url));
            reader.setLineMapper(new DefaultLineMapper<SimpleAnnotation>() {{
                setLineTokenizer(tokenizer);
                setFieldSetMapper(new BeanWrapperFieldSetMapper<SimpleAnnotation>(){{
                    setTargetType(SimpleAnnotation.class);
                }});
            }});
        } catch (IOException e) {
            getLog().error("Failed to read file: " + loadFrom + " " + e);
            return null;
        }
        return reader;
    }

    @Bean
    public AnnotationItemProcessor processor(){
        return new AnnotationItemProcessor();
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
                .<SimpleAnnotation, SimpleAnnotation> chunk(1000)
                .reader(reader())
                .processor(processor())
                .writer(flatFileItemWriter())
                .build();
    }

    private void validateHeaders(String[] headers) {
        boolean propType = false;
        boolean propValue = false;
        boolean semanticTag = false;
        for (String h : headers){
            if(h.contains("SEMANTIC_TAG")){
                semanticTag = true;
            }
            if (h.contains("PROPERTY_TYPE")){
                propType = true;
            }
            if (h.contains("PROPERTY_VALUE")){
                propValue = true;
            }
        }
        if(!(propType && propValue && semanticTag)){
            throw new IllegalArgumentException("Must include PROPERTY_TYPE, PROPERTY_VALUE, and SEMANTIC_TAG fields!");
        }
    }

    private String[] calculateHeaders(String[] headers, boolean writer){
        ArrayList<String> calcHeaders = new ArrayList<>();
        for (String h : headers){
            calcHeaders.add(h.toLowerCase());
        }
        if(writer && !calcHeaders.contains("annotationid")){
            calcHeaders.add("annotationid");
        }

        if(writer && !calcHeaders.contains("action")) {
            calcHeaders.add("action");
        }
        if(!writer && calcHeaders.contains("action")) {
            calcHeaders.remove("action");
        }
        String[] array = new String [calcHeaders.size()];
        return calcHeaders.toArray(array);
    }

    /*
    Calculates the field positions of the original headers to be included
    If position is missing, field is ignored
     */
    private int[] calculateFieldPositions(String[] originalHeaders, int finalLength) {
        int[] includedFieldPositions = new int[finalLength];

        int i = 0;
        int pos = 0;
        for (String h : originalHeaders){
            if (!h.replace("_", "").toLowerCase().equals("action")) {
                includedFieldPositions[i] = pos;
                i++;
            }
            pos++;
        }
        return includedFieldPositions;
    }

    private String calculateOutputHeader(String firstLine, String delimeter) {
        StringJoiner joiner = new StringJoiner(delimeter);
        joiner.add(firstLine);
        if(!firstLine.contains("ANNOTATION_ID")){
            joiner.add("ANNOTATION_ID");
        }
        if(!firstLine.contains("ACTION")){
            joiner.add("ACTION");
        }
        return joiner.toString();
    }

    public static void main(String[] args){
        SpringApplication.exit(SpringApplication.run(ZoomaCSVLoaderApplication.class, args));
    }
}
