package uk.ac.ebi.spot.zooma.service.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.PartialUpdate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationSummaryRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by olgavrou on 06/03/2017.
 */
@Service
public class AnnotationSummaryRepositoryService {

    @Autowired
    AnnotationSummaryRepository summaryRepository;

    @Autowired
    SolrTemplate solrTemplate;

    @Value("${solr.core}")
    String solrCore;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void save(AnnotationSummary summary) {
        boolean updated;
        try {
            updated = update(summary);
        } catch (IOException | SolrServerException e) {
            getLog().debug("Update Solr AnnotationSummary could not be accessed!");
            throw new IllegalStateException("Solr could not be accessed on update! " + e);
        }
        if(!updated){
            AnnotationSummary saved = summaryRepository.save(summary);
            getLog().info("New Solr AnnotationSummary: " + saved.getId());
        }
    }

    public boolean update(AnnotationSummary summary) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        StringJoiner semTagQuery = new StringJoiner(" AND semanticTag: ");
        for(String s : summary.getSemanticTag()){
            semTagQuery.add("\"" + s + "\"");
        }

        query.set("q", "propertyValueStr:\"" + summary.getPropertyValue() + "\" AND " +
                "semanticTag:" + semTagQuery.toString() + " AND propertyType: \"" + summary.getPropertyType() + "\"");

        QueryResponse response = solrTemplate.getSolrClient().query(this.solrCore, query);
        List<AnnotationSummary> results = response.getBeans(AnnotationSummary.class);
        if(results.isEmpty()){
            return false;
        }

        AnnotationSummary existingAnn = results.get(0); //should be exactly one

        if (!existingAnn.equals(summary)){
            return false;
        }

        PartialUpdate partialUpdate = new PartialUpdate("id", existingAnn.getId());

        Collection<String> existingS = existingAnn.getSource();
        String source = summary.getSource().iterator().next(); //new annotationSummary when added will have one source

        if (!existingS.contains(source)){
            partialUpdate.addValueToField("source", source);
            partialUpdate.increaseValueOfField("sourceNum", 1);
        }

        partialUpdate.addValueToField("mongoid", summary.getMongoid());
        if(existingAnn.getQuality() < summary.getQuality()){
            //new annotationSummary has higher quality
            //will be stored as summary's quality
            partialUpdate.setValueOfField("quality", summary.getQuality());
            getLog().info("Updated document quality:" + existingAnn.getId());
        }

        partialUpdate.increaseValueOfField("votes", 1);
        solrTemplate.saveBean(partialUpdate);
        solrTemplate.commit();

        getLog().info("Solr AnnotationSummary Updated: " + existingAnn.getId());
        return true;
    }

}
