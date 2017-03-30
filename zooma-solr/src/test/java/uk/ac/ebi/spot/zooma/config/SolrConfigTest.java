package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationSummaryRepository;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationSummaryRepositoryService;
import uk.ac.ebi.spot.zooma.utils.Scorer;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Configuration
@ComponentScan("uk.ac.ebi.spot")
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.zooma.repository.solr")
public class SolrConfigTest {

        @Value("${solr.host}")
        private String solrHost;

        @Value("${solr.core}")
        String solrCore;

        @Autowired
        private AnnotationSummaryRepository repository;

        @Autowired
        Scorer<AnnotationSummary> scorer;

        @Bean
        public SolrClient solrClient() {
                return new HttpSolrClient(solrHost);
        }

        @Bean
        public SolrTemplate solrTemplate(){
                TestCustomSolrTemplate template = new TestCustomSolrTemplate(solrClient(), solrCore);
//        SolrTemplate template = new SolrTemplate(solrClient());
                return template;
        }

//        @Bean
//        AnnotationSummaryRepositoryService repositoryService(){
//                return new AnnotationSummaryRepositoryService(repository, solrTemplate(), solrHost, scorer);
//        }
}
