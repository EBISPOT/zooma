package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.solr.CustomSolrTemplate;

/**
 * Created by olgavrou on 13/02/2017.
 */
@Configuration
public class SolrTemplateConfig {

    @Value("${solr.host}")
    String solrHost;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient(solrHost);
    }

    @Bean
    SolrTemplate solrTemplate() {
        CustomSolrTemplate solrTemplate = new CustomSolrTemplate(solrClient());
//        SolrTemplate solrTemplate = new SolrTemplate(solrClient());
        return solrTemplate;
    }

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
