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
import uk.ac.ebi.spot.zooma.utils.Scorer;
import uk.ac.ebi.spot.zooma.utils.SolrUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by olgavrou on 06/03/2017.
 */
@Service
public class AnnotationSummaryRepositoryService {

    private AnnotationSummaryRepository summaryRepository;

    private SolrTemplate solrTemplate;

    private String solrCore;

    Scorer<AnnotationSummary> scorer;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public AnnotationSummaryRepositoryService(AnnotationSummaryRepository summaryRepository,
                                              SolrTemplate solrTemplate,
                                              @Value("${solr.core}") String solrCore,
                                              Scorer<AnnotationSummary> scorer) {
        this.summaryRepository = summaryRepository;
        this.solrTemplate = solrTemplate;
        this.solrCore = solrCore;
        this.scorer = scorer;
    }

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
            getLog().info("New Solr AnnotationSummary: " + saved.toString());
        }
    }

    public void replace(AnnotationSummary summary, String replaces) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "mongoid:\"" + replaces + "\"");
        solrQuery.set("fl","*,score");
        QueryResponse response = null;
        try {
            response = solrTemplate.getSolrClient().query(this.solrCore, solrQuery);
        } catch (IOException | SolrServerException e) {
            getLog().debug("Replace Solr AnnotationSummary could not be accessed!");
            throw new IllegalStateException("Solr could not be accessed on update! " + e);
        }
        List<AnnotationSummary> results = response.getBeans(AnnotationSummary.class);
        if(results.isEmpty()){
            getLog().error("Mongo document that has been replaced does not exist in solr! " + replaces);
            return;
        }
        if (results.size() > 1){
            getLog().error("Mongo id  was found in more than one annotation summary! " + replaces);
            return;
        }
        AnnotationSummary oldSummary = results.get(0);
        if(!summary.getPropertyValue().equals(oldSummary.getPropertyValue())){
            //TODO: update property value
            //TODO: update property type
            return;
        } else if(!SolrUtils.listEqualsNoOrder(summary.getSemanticTag(), oldSummary.getSemanticTag())){
            //setting votes and source num same as summary being replaced for scoring to be fair
            summary.setSourceNum(oldSummary.getSourceNum());
            summary.setVotes(oldSummary.getVotes());
            //save new annotation summary that should be favoured over because it is more resent
            summaryRepository.save(summary);
            getLog().info("New annotation summary {} replacing {} ", summary, oldSummary.getId());
        }
    }

    public List<AnnotationSummary>  findByPropertyTypeAndValue(List<String> sourceNames, String propertyType, String propertyValue) throws IOException, SolrServerException {

        StringJoiner query = new StringJoiner(" AND ");
        query.add("propertyValue:\"" + propertyValue + "\"");
        if(propertyType != null){
            query.add("propertyType:\"" + propertyType + "\"");
        }
        if(sourceNames != null && !sourceNames.isEmpty()){
            for (String source : sourceNames){
                query.add("source:\"" + source + "\"");
            }
        }

        StringJoiner q2 = new StringJoiner(" OR ");
        q2.add(query.toString());
        q2.add("propertyValueStr:\"" + propertyValue + "\"^100");
        q2.add("propertyValue:" + propertyValue);

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", q2.toString());
        solrQuery.set("defType", "edismax");
        solrQuery.set("bf","product(votes,sourceNum,quality)^0.0001");
        solrQuery.set("fl","*,score");

        QueryResponse response = solrTemplate.getSolrClient().query(solrQuery);
        List<AnnotationSummary> results = response.getBeans(AnnotationSummary.class);

        List<AnnotationSummary> annotationSummaries = new ArrayList<>();

        int totalDocumentsFound = 0;
        float maxSolrScore = 0.0f;

        for(AnnotationSummary annotation : results){
            if (annotation.getScore() > maxSolrScore){
                maxSolrScore = annotation.getScore();
            }

            totalDocumentsFound = totalDocumentsFound + annotation.getVotes();
        }

        for(AnnotationSummary summary : results){

            annotationSummaries.add(new AnnotationSummary(summary.getPropertyType(), summary.getPropertyValue(), summary.getSemanticTag(),
                    summary.getMongoid(), summary.getStrongestMongoid(), summary.getSource(),
                    summary.getScore(), // solr score for quality
                    summary.getVotes(), summary.getSourceNum(),
                    summary.getLastModified()));
        }

        Map<AnnotationSummary, Float> annotationsToScore = scorer.score(annotationSummaries, propertyValue);


        List<AnnotationSummary> returnSumms = new ArrayList<>();
        for (AnnotationSummary as : annotationsToScore.keySet()){
            // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
            float normalizedScore = normScore(maxSolrScore, annotationsToScore.get(as));
            as.setQuality(normalizedScore);
            returnSumms.add(as);
        }

        return returnSumms;
    }

    private boolean update(AnnotationSummary summary) throws IOException, SolrServerException {

        Optional<AnnotationSummary> retrieve = retrieveExistingAnnotationSummary(summary);
        AnnotationSummary existingAnn;
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

        getLog().info("Solr AnnotationSummary Updated: " + existingAnn.getId());
        return true;
    }

    private Optional<AnnotationSummary> retrieveExistingAnnotationSummary(AnnotationSummary summary) throws IOException, SolrServerException {
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
            return Optional.empty();
        }

        AnnotationSummary existingAnn = results.get(0); //should be exactly one

        if (!existingAnn.equals(summary)){
            return Optional.empty();
        }
        return Optional.of(existingAnn);
    }

    private float normScore(Float msbs, Float sas) {
        float dx = 100 * ((msbs - sas) / msbs);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

}
