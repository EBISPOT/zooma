package uk.ac.ebi.spot.zooma.config;


import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;


@Configuration
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot")
public class SolrContext {

    @Value("${solr.host}")
    String solrHost;

    @Bean
    public HttpSolrClient solrServer() {
        return new HttpSolrClient(solrHost);
    }

    @Bean
    SolrTemplate solrTemplate() {
        SolrTemplate solrTemplate = new SolrTemplate(solrServer());
        solrTemplate.setSolrCore("annotationsummaries");
        return solrTemplate;
    }

}
