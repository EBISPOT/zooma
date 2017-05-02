package uk.ac.ebi.spot.zooma.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.CollectionCallback;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.SolrConverter;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.test.context.TestPropertySource;

/**
 * Created by olgavrou on 27/03/2017.
 */
public class TestCustomSolrTemplate extends SolrTemplate {

    String solrCore;

    public TestCustomSolrTemplate(SolrClient solrClient) {
        super(solrClient);
    }

    public TestCustomSolrTemplate(SolrClient solrClient, String core) {
        super(solrClient, core);
        this.solrCore = core;
    }

    public TestCustomSolrTemplate(SolrClient solrClient, String core, RequestMethod requestMethod) {
        super(solrClient, core, requestMethod);
    }

    public TestCustomSolrTemplate(SolrClientFactory solrClientFactory) {
        super(solrClientFactory);
    }

    public TestCustomSolrTemplate(SolrClientFactory solrClientFactory, String defaultCore) {
        super(solrClientFactory, defaultCore);
    }

    public TestCustomSolrTemplate(SolrClientFactory solrClientFactory, RequestMethod requestMethod) {
        super(solrClientFactory, requestMethod);
    }

    public TestCustomSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter) {
        super(solrClientFactory, solrConverter);
    }

    public TestCustomSolrTemplate(SolrClientFactory solrClientFactory, SolrConverter solrConverter, RequestMethod defaultRequestMethod) {
        super(solrClientFactory, solrConverter, defaultRequestMethod);
    }

    @Override
    public <T> T execute(String collection, CollectionCallback<T> action) {
        return super.execute(null, action);
    }
}