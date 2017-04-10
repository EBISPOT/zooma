package uk.ac.ebi.spot.zooma.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import uk.ac.ebi.spot.zooma.messaging.solr.AnnotationSubmissionReceiver;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryService;


@Configuration
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.zooma.repository.solr")
public class SolrConfig {

    @Value("${solr.host}")
    String solrHost;

    @Value("${rabbitmq.activate}")
    Boolean activateRabbit;

    @Autowired
    AnnotationRepositoryService summaryRepositoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient(solrHost);
    }

    @Bean
    public SolrTemplate solrTemplate(){
        CustomSolrTemplate template = new CustomSolrTemplate(solrClient());
//        SolrTemplate template = new SolrTemplate(solrClient());
        return template;
    }

    @Bean
    AnnotationSubmissionReceiver receiver(AnnotationRepositoryService service, ObjectMapper mapper){
        if(activateRabbit) {
            return new AnnotationSubmissionReceiver(service, mapper);
        } else {
            return null;
        }
    }

}
