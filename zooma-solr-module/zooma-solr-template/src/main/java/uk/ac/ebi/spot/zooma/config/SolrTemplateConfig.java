package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olgavrou on 13/02/2017.
 */
@Configuration
public class SolrTemplateConfig {

    @Value("${solr.host}")
    String solrHost;

    @Bean
    public HttpSolrClient solrServer() {
        return new HttpSolrClient(solrHost);
    }

    @Bean
    SolrTemplate solrTemplate() {
        SolrTemplate solrTemplate = new SolrTemplate(solrServer());
        solrTemplate.setSolrCore("annotations");
        return solrTemplate;
    }

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
