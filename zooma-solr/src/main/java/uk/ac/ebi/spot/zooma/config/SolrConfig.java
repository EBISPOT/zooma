package uk.ac.ebi.spot.zooma.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.data.solr.server.support.MulticoreSolrClientFactory;
import uk.ac.ebi.spot.zooma.messaging.solr.AnnotationSubmissionReceiver;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.model.solr.Recommendation;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationRepository;
import uk.ac.ebi.spot.zooma.repository.solr.RecommendationRepository;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryServiceWrite;


@Configuration
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.zooma.repository.solr", basePackageClasses = {Recommendation.class, Annotation.class}, multicoreSupport = true)
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    String solrHost;

    @Value("${spring.rabbitmq.activate}")
    Boolean activateRabbit;

    // Factory creates SolrServer instances for base url when requesting server
    // for specific core.
    @Bean
    public SolrClientFactory solrServerFactory() {
        return new MulticoreSolrClientFactory(new HttpSolrClient(solrHost));
    }

    @Bean(name = "annotationsSolrTemplate")
    public SolrTemplate annotationsSolrTemplate(){
        SolrTemplate template = new SolrTemplate(solrServerFactory(), "annotations");
        template.setSolrCore("annotations");
        return template;
    }

    @Bean
    public SolrTemplate recommendationSolrTemplate(){
        SolrTemplate template = new SolrTemplate(solrServerFactory(), "recommendations");
        template.setSolrCore("recommendations");
        return template;
    }

    @Bean
    public AnnotationRepository annotationSolrRepository() throws Exception {
        return new SolrRepositoryFactory(annotationsSolrTemplate())
                .getRepository(AnnotationRepository.class);
    }

    @Bean
    public RecommendationRepository recommendationSolrRepository() throws Exception {
        return new SolrRepositoryFactory(recommendationSolrTemplate())
                .getRepository(RecommendationRepository.class);
    }

    @Bean
    AnnotationSubmissionReceiver receiver(AnnotationRepositoryServiceWrite service, ObjectMapper mapper){
        if(activateRabbit) {
            return new AnnotationSubmissionReceiver(service, mapper);
        } else {
            return null;
        }
    }

}
