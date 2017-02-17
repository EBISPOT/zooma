package uk.ac.ebi.spot.zooma.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.FacetPivotFieldEntry;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.model.SolrBaseAnnotation;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.utils.CriteriaGenerator;
import uk.ac.ebi.spot.zooma.utils.Scorer;


import java.io.IOException;
import java.util.*;

/**
 * Created by olgavrou on 13/02/2017.
 */
@Service
public class SolrSelectAnnotationsService {

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    Scorer<AnnotationSummary> scorer;

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue) {

        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyValue", propertyValue);

        try {
            return  findAnnotationSummariesByCriteria(criteriaMap, null, null, propertyValue);
        } catch (IOException | SolrServerException e) {
            return new ArrayList<>();
        }
    }

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue, List<String> sourceNames) {

        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyValue", propertyValue);


        try {
            return  findAnnotationSummariesByCriteria(criteriaMap, sourceNames, null, propertyValue);
        } catch (IOException | SolrServerException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Defines the criteria based on the propertyValue and propertyType and then performs a search
     *
     * @param propertyType the type of the term that will be searched for,
     *                     and that that can help boost the score of the results
     * @param propertyValue the term that will be searched for

     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyType", propertyType);
        criteriaMap.put("propertyValue", propertyValue);
        try {
            return findAnnotationSummariesByCriteria(criteriaMap, null, propertyType, propertyValue);
        } catch (IOException | SolrServerException e) {
            return new ArrayList<>();
        }
    }

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue, List<String> sourceNames) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyType", propertyType);
        criteriaMap.put("propertyValue", propertyValue);
        try {
            return findAnnotationSummariesByCriteria(criteriaMap, sourceNames, propertyType, propertyValue);
        } catch (IOException | SolrServerException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Uses solr pivoting to search for a term value in the {@link SolrBaseAnnotation} documents stored in
     * the Solr server.
     * The results are pivoted, so essentially grouped, by semanticTag, then by propertyValueStr (a String copy of the indexed propertyValue
     * so the pivot group won't break on the different value words), then by source and lastly by id.
     * The result will be seperate semanticTag, each will contain a group of their different propertyValueStr, and again each propertyValueStr
     * will contain a group of the different sources and again, each source will contain the Document ids.
     *
     * @param criteriaMap the map from which the criteria will be made for solr will query, e.g.: <"propertyValue", "<a property value>">
     *                    can result to criteria: where("propertyValue").is("a property value")
     * @param sourceNames list of the sources we want to restrict the search to, NULL when none specified
     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    private List<AnnotationSummary>  findAnnotationSummariesByCriteria(Map<String, String> criteriaMap, List<String> sourceNames, String propertyType, String propertyValue) throws IOException, SolrServerException {

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

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query.toString());
        solrQuery.set("defType", "edismax");
        solrQuery.set("bf","product(votes,sourceNum,quality)");
        solrQuery.set("fl","*,score");

        QueryResponse response = solrTemplate.getSolrClient().query(solrQuery);
        List<Annotation> results = response.getBeans(Annotation.class);

        List<AnnotationSummary> annotationSummaries = new ArrayList<>();

        int totalDocumentsFound = 0;
        float maxSolrScore = 0.0f;

        for(Annotation annotation : results){
            if (annotation.getScore() > maxSolrScore){
                maxSolrScore = annotation.getScore() + annotation.getQuality();
            }

            totalDocumentsFound = totalDocumentsFound + annotation.getVotes();
        }

        for(Annotation annotation : results){

//            float sourceNumber = annotation.getSource().size();
//            float numOfDocs = annotation.getVotes();
//            float topQuality = annotation.getQuality();
//            float normalizedFreq = 1.0f + (totalDocumentsFound > 0 ? (numOfDocs / totalDocumentsFound) : 0);
//            float normalizedSolrScore = 1.0f + annotation.getScore()/maxSolrScore;
//            float score = (topQuality + sourceNumber) * normalizedSolrScore * normalizedFreq;

            annotationSummaries.add(new AnnotationSummary(annotation.getPropertyType(), annotation.getPropertyValue(), annotation.getSemanticTag(),
                    annotation.getMongoid(), annotation.getSource(),
                    annotation.getScore()));
        }

        Map<AnnotationSummary, Float> annotationsToScore = scorer.score(annotationSummaries, propertyValue);

        float minSolrScore = Collections.min(annotationsToScore.values());
        if(minSolrScore == maxSolrScore){
            minSolrScore = 0.0f;
        }

        List<AnnotationSummary> returnSumms = new ArrayList<>();
        for (AnnotationSummary as : annotationsToScore.keySet()){
            // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
            float normalizedScore = normScore(as.getQuality(), annotationsToScore.get(as), maxSolrScore, minSolrScore);
            as.setQuality(normalizedScore);
            returnSumms.add(as);
        }

        return returnSumms;
    }

    private float normScore(Float initScore, Float score, float max, float min) {

        float d = max - score; //(((initScore - score)/(initScore)));
        float m = (max - (max*d));
        float s = (max - d)/max;

//        if ((s - min) < 0) {
//            return 50;
//        }
//        else {
            float n = 50 + (50 * (d - min)/(max - min));
            return n;
//        }
    }

    public boolean update(Annotation annotation) throws IOException, SolrServerException {

        SolrQuery query = new SolrQuery();
        StringJoiner semTagQuery = new StringJoiner(" AND semanticTag: ");
        for(String s : annotation.getSemanticTag()){
            semTagQuery.add("\"" + s + "\"");
        }

        query.set("q", "propertyValueStr:\"" + annotation.getPropertyValue() + "\" AND " +
                "semanticTag:" + semTagQuery.toString() + " AND propertyType: \"" + annotation.getPropertyType() + "\"");

        QueryResponse response = solrTemplate.getSolrClient().query(query);
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

        partialUpdate.increaseValueOfField("votes", 1);
        solrTemplate.saveBean(partialUpdate);
        solrTemplate.commit();

        return true;
    }

}
