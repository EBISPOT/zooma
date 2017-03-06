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
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by olgavrou on 06/03/2017.
 */
@Service
public class AnnotationRepositoryService {

    @Autowired
    AnnotationRepository annotationRepository;

    @Autowired
    SolrTemplate solrTemplate;

    @Value("${solr.core}")
    String solrCore;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void save(Annotation annotation) {
        boolean updated;
        try {
            updated = update(annotation);
        } catch (IOException | SolrServerException e) {
            getLog().debug("Update Solr Annotation could not be accessed!");
            throw new IllegalStateException("Solr could not be accessed on update! " + e);
        }
        if(!updated){
            Annotation saved = annotationRepository.save(annotation);
            getLog().info("New Solr Annotation: " + saved.getId());
        }
    }

    public boolean update(Annotation annotation) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        StringJoiner semTagQuery = new StringJoiner(" AND semanticTag: ");
        for(String s : annotation.getSemanticTag()){
            semTagQuery.add("\"" + s + "\"");
        }

        query.set("q", "propertyValueStr:\"" + annotation.getPropertyValue() + "\" AND " +
                "semanticTag:" + semTagQuery.toString() + " AND propertyType: \"" + annotation.getPropertyType() + "\"");

        QueryResponse response = solrTemplate.getSolrClient().query(this.solrCore, query);
        List<Annotation> results = response.getBeans(Annotation.class);
        if(results.isEmpty()){
            return false;
        }

        Annotation existingAnn = results.get(0); //should be exactly one

        if (!existingAnn.equals(annotation)){
            return false;
        }

        PartialUpdate partialUpdate = new PartialUpdate("id", existingAnn.getId());

        Collection<String> existingS = existingAnn.getSource();
        String source = annotation.getSource().iterator().next(); //new annotation when added will have one source

        if (!existingS.contains(source)){
            partialUpdate.addValueToField("source", source);
            partialUpdate.increaseValueOfField("sourceNum", 1);
        }

        partialUpdate.addValueToField("mongoid", annotation.getMongoid());
        if(existingAnn.getQuality() < annotation.getQuality()){
            //new annotation has higher quality
            //will be stored as summary's quality
            partialUpdate.setValueOfField("quality", annotation.getQuality());
            log.info("Updated document quality:" + existingAnn.getId());
        }

        partialUpdate.increaseValueOfField("votes", 1);
        solrTemplate.saveBean(partialUpdate);
        solrTemplate.commit();

        getLog().info("Solr Annotation Updated: " + existingAnn.getId());
        return true;
    }

}
