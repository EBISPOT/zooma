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
import uk.ac.ebi.spot.zooma.utils.Scorer;
import uk.ac.ebi.spot.zooma.utils.SolrUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by olgavrou on 06/03/2017.
 */
@Service
public class AnnotationRepositoryService {

    private AnnotationRepository summaryRepository;

    private SolrTemplate solrTemplate;

    private String solrCore;

    Scorer<Annotation> scorer;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Autowired
    public AnnotationRepositoryService(AnnotationRepository summaryRepository,
                                       SolrTemplate solrTemplate,
                                       @Value("${spring.data.solr.core}") String solrCore,
                                       Scorer<Annotation> scorer) {
        this.summaryRepository = summaryRepository;
        this.solrTemplate = solrTemplate;
        this.solrCore = solrCore;
        this.scorer = scorer;
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
        if(!summary.getPropertyValue().equals(oldSummary.getPropertyValue())){
            summaryRepository.save(summary);
            String source = summary.getSource().iterator().next(); // new summary should have exactly one source
            if (oldSummary.getSource().contains(source)){
                if(oldSummary.getVotes() == 1){
                    summaryRepository.delete(oldSummary);
                    getLog().debug("Old summary {} replaced and deleted by: ", oldSummary.getId(), summary );
                } else {
                    PartialUpdate partialUpdate = new PartialUpdate("id", oldSummary.getId());
                    partialUpdate.setValueOfField("votes", (oldSummary.getVotes() - 1));
                    Collection<String> oldSources = oldSummary.getSource();
                    if (oldSources.size() > 1){
                        oldSources.remove(source);
                        partialUpdate.setValueOfField("source", oldSources);
                        int sNum = (oldSummary.getSourceNum() - 1);
                        partialUpdate.setValueOfField("sourceNum", sNum);
                    }
                    solrTemplate.saveBean(partialUpdate);
                    solrTemplate.commit();
                }
            }
        } else if(!SolrUtils.listEqualsNoOrder(summary.getSemanticTag(), oldSummary.getSemanticTag())){
            //setting votes and source num same as summary being replaced for scoring to be fair
//            summary.setSourceNum(oldSummary.getSourceNum());
//            summary.setVotes(oldSummary.getVotes());
            //save new annotation summary that should be favoured over because it is more resent
            summaryRepository.save(summary);
        }
        getLog().info("New annotation summary {} replacing {} ", summary, oldSummary.getId());
    }

    public List<Annotation>  findByPropertyTypeAndValue(List<String> sourceNames, String propertyType, String propertyValue) throws IOException, SolrServerException {

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
        q2.add("propertyValueStr:\"" + propertyValue + "\"^10");
        q2.add("propertyValue:" + propertyValue);

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", q2.toString());
        solrQuery.setRows(1);
        solrQuery.addSort("lastModified", SolrQuery.ORDER.desc);
        solrQuery.setIncludeScore(true);
        QueryResponse response = solrTemplate.getSolrClient().query(solrQuery);
        List<Annotation> results = response.getBeans(Annotation.class);
        LocalDateTime localDateTime = null;
        if (!results.isEmpty()){
            Date dateTime = results.get(0).getLastModified();
            localDateTime = dateTime.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        }

        SolrQuery q = new SolrQuery();
        if (localDateTime != null){
            q.set("bf", "sum(div(1,sum(0.001,ms(\"" + localDateTime.toString() + "Z\",lastModified))),product(sum(votes,quality,sourceNum),0.001))^0.001");
        } else {
            q.set("bf","sum(votes,sourceNum,quality)^0.001");
        }

        q.set("q", q2.toString());
        q.set("defType", "edismax");
        q.setIncludeScore(true);

        response = solrTemplate.getSolrClient().query(q);
        results = response.getBeans(Annotation.class);

        List<Annotation> annotationSummaries = new ArrayList<>();

        int totalDocumentsFound = 0;
        float maxSolrScore = 0.0f;

        for(Annotation annotation : results){
            if (annotation.getScore() > maxSolrScore){
                maxSolrScore = annotation.getScore();
            }

            totalDocumentsFound = totalDocumentsFound + annotation.getVotes();
        }

        for(Annotation summary : results){

            annotationSummaries.add(new Annotation(summary.getPropertyType(), summary.getPropertyValue(), summary.getSemanticTag(),
                    summary.getMongoid(), summary.getStrongestMongoid(), summary.getSource(),
                    summary.getScore(), // solr score for quality
                    summary.getVotes(), summary.getSourceNum(),
                    summary.getLastModified()));
        }

        Map<Annotation, Float> annotationsToScore = scorer.score(annotationSummaries, propertyValue);


        List<Annotation> returnSumms = new ArrayList<>();
        for (Annotation as : annotationsToScore.keySet()){
            // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
            float normalizedScore = normScore(maxSolrScore, annotationsToScore.get(as));
            as.setQuality(normalizedScore);
            returnSumms.add(as);
        }

        return returnSumms;
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

    private float normScore(Float msbs, Float sas) {
        float dx = 100 * ((msbs - sas) / msbs);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

}
