package uk.ac.ebi.spot.zooma.config;

        import org.apache.solr.client.solrj.impl.HttpSolrClient;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.ComponentScan;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.data.solr.core.SolrTemplate;

/**
 * Created by olgavrou on 13/10/2016.
 */
@Configuration
@ComponentScan("uk.ac.ebi.spot")
public class SolrConfig {
        @Bean
        SolrTemplate solrTemplate(){
                return new SolrTemplate(new HttpSolrClient(""));
        }
}
