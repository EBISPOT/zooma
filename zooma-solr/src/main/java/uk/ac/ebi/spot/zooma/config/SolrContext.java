package uk.ac.ebi.spot.zooma.config;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import uk.ac.ebi.spot.zooma.model.solr.CustomSolrTemplate;


@Configuration
@EnableSolrRepositories(basePackages = "uk.ac.ebi.spot.zooma.repository.solr")
public class SolrContext {

    @Value("${solr.host}")
    String solrHost;

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

}
