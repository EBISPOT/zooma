package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.*;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.server.SolrClientFactory;

/**
 * Temporary custom SolrTemplate class to override the spring bug
 * for spring data solr
 * Issue: https://github.com/spring-projects/spring-boot/issues/8327
 * Created by olgavrou on 28/02/2017.
 */
public class CustomSolrTemplate extends SolrTemplate {

    @Value("${solr.core}")
    String solrCore;

    public CustomSolrTemplate(SolrClient solrClient) {
        super(solrClient);
    }

    public CustomSolrTemplate(SolrClient solrClient, String core) {
        super(solrClient, core);
    }

    public CustomSolrTemplate(SolrClient solrClient, String core, RequestMethod requestMethod) {
        super(solrClient, core, requestMethod);
    }

    public CustomSolrTemplate(SolrClientFactory solrClientFactory) {
        super(solrClientFactory);
    }

    public CustomSolrTemplate(SolrClientFactory solrClientFactory, String defaultCore) {
        super(solrClientFactory, defaultCore);
    }

    public CustomSolrTemplate(SolrClientFactory solrClientFactory, RequestMethod requestMethod) {
        super(solrClientFactory, requestMethod);
    }

    public CustomSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter) {
        super(solrClientFactory, solrConverter);
    }

    public CustomSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter, RequestMethod defaultRequestMethod) {
        super(solrClientFactory, solrConverter, defaultRequestMethod);
    }

    @Override
    public <T> T execute(String collection, CollectionCallback<T> action) {
        return super.execute(this.solrCore, action);
    }
}
