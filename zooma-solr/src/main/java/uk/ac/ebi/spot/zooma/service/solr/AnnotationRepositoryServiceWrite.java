package uk.ac.ebi.spot.zooma.service.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.PartialUpdate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.config.AnnotationSolrTemplate;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by olgavrou on 20/04/2017.
 */
@Service
public class AnnotationRepositoryServiceWrite {
    private AnnotationRepository summaryRepository;

    private AnnotationSolrTemplate solrTemplate;

    private String solrCore;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Autowired
    public AnnotationRepositoryServiceWrite(@Qualifier("annotationSolrRepository") AnnotationRepository summaryRepository,
                                            AnnotationSolrTemplate solrTemplate,
                                            @Value("${spring.data.solr.core}") String solrCore) {
        this.summaryRepository = summaryRepository;
        this.solrTemplate = solrTemplate;
        this.solrCore = solrCore;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void save(Annotation summary) {
        boolean updated;
        try {
            updated = update(summary);
        } catch (IOException | SolrServerException e) {
            getLog().debug("Update Solr Annotation could not be accessed!");
            throw new IllegalStateException("Solr could not be accessed on update! " + e);
        }
        if(!updated){
            Annotation saved = summaryRepository.save(summary);
            getLog().info("New Solr Annotation: " + saved.toString());
        }
    }

    public void replace(Annotation summary, String replaces) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "mongoid:\"" + replaces + "\"");
        solrQuery.set("fl","*,score");
        QueryResponse response = null;
        try {
            response = solrTemplate.getSolrClient().query(this.solrCore, solrQuery);
        } catch (IOException | SolrServerException e) {
            getLog().debug("Replace Solr Annotation could not be accessed!");
            throw new IllegalStateException("Solr could not be accessed on update! " + e);
        }
        List<Annotation> results = response.getBeans(Annotation.class);
        if(results.isEmpty()){
            getLog().error("Mongo document that has been replaced does not exist in solr! " + replaces);
            return;
        }
        if (results.size() > 1){
            getLog().error("Mongo id  was found in more than one annotation summary! " + replaces);
            return;
        }

        Annotation oldSummary = results.get(0);
//        summary.setSourceNum(oldSummary.getSourceNum());
//        summary.setVotes(oldSummary.getVotes());
        summaryRepository.save(summary);

        getLog().info("New annotation summary {} replacing {} ", summary, oldSummary.getId());
    }


    private boolean update(Annotation summary) throws IOException, SolrServerException {

        Optional<Annotation> retrieve = retrieveExistingAnnotationSummary(summary);
        Annotation existingAnn;
        if (!retrieve.isPresent()){
            return false;
        } else {
            existingAnn = retrieve.get();
        }

        PartialUpdate partialUpdate = new PartialUpdate("id", existingAnn.getId());

        Collection<String> existingS = existingAnn.getSource();
        String source = summary.getSource().iterator().next(); //new annotationSummary when added will have one source
        if (!existingS.contains(source)){
            partialUpdate.addValueToField("source", source);
            partialUpdate.increaseValueOfField("sourceNum", 1);
        }

        Collection<String> existingTopics = existingAnn.getTopic();
        Collection<String> newTopics = new ArrayList<>();
        summary.getTopic().stream().forEach(s -> {
                if (!existingTopics.contains(s)){
                    newTopics.add(s);
                }
            }
        );
        if(!newTopics.isEmpty()) {
            partialUpdate.addValueToField("topic", newTopics);
        }

        partialUpdate.addValueToField("mongoid", summary.getMongoid());
        if(existingAnn.getQuality() < summary.getQuality()){
            //new annotationSummary has higher quality
            //will be stored as summary's quality
            partialUpdate.setValueOfField("quality", summary.getQuality());
            getLog().info("Updated document quality:" + existingAnn.getId());
        }

        partialUpdate.increaseValueOfField("votes", 1);

        LocalDateTime dateTime = LocalDateTime.now();
        String d = dateTime.format(formatter);
        partialUpdate.setValueOfField("lastModified", d);
        solrTemplate.saveBean(partialUpdate);
        solrTemplate.commit();

        getLog().info("Solr Annotation Updated: " + existingAnn.getId());
        return true;
    }

    private Optional<Annotation> retrieveExistingAnnotationSummary(Annotation summary) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        StringJoiner semTagQuery = new StringJoiner(" AND semanticTag: ");
        for(String s : summary.getSemanticTag()){
            semTagQuery.add("\"" + s + "\"");
        }

        query.set("q", "propertyValueStr:\"" + summary.getPropertyValue() + "\" AND " +
                "semanticTag:" + semTagQuery.toString() + " AND propertyType: \"" + summary.getPropertyType() + "\"");

        QueryResponse response = solrTemplate.getSolrClient().query(this.solrCore, query);
        List<Annotation> results = response.getBeans(Annotation.class);
        if(results.isEmpty()){
            return Optional.empty();
        }

        Annotation existingAnn = results.get(0); //should be exactly one

        if (!existingAnn.equals(summary)){
            return Optional.empty();
        }
        return Optional.of(existingAnn);
    }

}
