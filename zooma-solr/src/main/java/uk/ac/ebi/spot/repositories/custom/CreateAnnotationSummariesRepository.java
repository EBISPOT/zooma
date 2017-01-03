package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.FacetPivotFieldEntry;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.model.SolrAnnotationSummary;

import java.util.*;

/**
 * This repository takes information about the {@link SolrAnnotation}s that where queried and pivoted
 * and calculates {@link AnnotationSummary}s
 *
 * Created by olgavrou on 31/10/2016.
 */
@Repository
public class CreateAnnotationSummariesRepository {

    @Autowired
    SolrTemplate solrTemplate;

    /**
     * Converts {@link SolrAnnotation}s into {@link AnnotationSummary}s and calculated the summary scores using the pivot information.
     * Each {@link AnnotationSummary} will be represented by the strongest {@link uk.ac.ebi.spot.model.Annotation} Document.
     *
     * @param content the list of Annotations that where found when searching for the propertyValue
     * @param pivots the pivot that gives information about the "groupings" of the annotations and will be used to create scores for the summaries
     * @param totalDocumentsFound the total documents returned when the propertyValue was queried
     * @return a list of the calculated {@link AnnotationSummary}s
     */
    public List<AnnotationSummary> convertToAnnotationSumaries(List<SolrAnnotation> content, List<FacetPivotFieldEntry> pivots, long totalDocumentsFound){

        Map<String, Float> idToSolrScore = new HashMap<>();

        float maxScore = 0.0f;

        List<AnnotationSummary> annotations = new ArrayList<>();


        for (SolrAnnotation annotation : content){
            idToSolrScore.put(annotation.getId(), annotation.getScore());
            if (annotation.getScore() > maxScore){
                maxScore = annotation.getScore();
            }
        }

        for (FacetPivotFieldEntry propertyValues : pivots){
            List<FacetPivotFieldEntry> semanticTags = propertyValues.getPivot();
            for (FacetPivotFieldEntry semanticTag : semanticTags){
                List<FacetPivotFieldEntry> sources = semanticTag.getPivot();

                Criteria criteria1 = null;
                List<String> ids = new ArrayList<>(); //ids are collected from both sources
                for (FacetPivotFieldEntry source : sources){
                    List<FacetPivotFieldEntry> sourceIds = source.getPivot();
                    int idCount = 0;
                    for (FacetPivotFieldEntry sourceId : sourceIds){
                        ids.add(sourceId.getValue());

                        idCount++;
                        if (criteria1 == null){
                            criteria1 = new Criteria("id").is(sourceId.getValue());
                        } else {
                            //search can't handle queries that are too long TODO: fix this
                            if (idCount < 100) {
                                criteria1 = criteria1.or(new Criteria("id").is(sourceId.getValue()));
                            }
                        }
                    }
                }

                //create the query to get the biggest quality document and its quality score
                Query idQuery = new SimpleQuery();
                idQuery.addCriteria(criteria1);
                idQuery.setRows(1);
                idQuery.addSort(new Sort(Sort.Direction.DESC, "quality"));
                ScoredPage<SolrAnnotation> docs = solrTemplate.queryForPage(idQuery, SolrAnnotation.class);
                List<SolrAnnotation> annotationsFromIds = docs.getContent();
                if (annotationsFromIds == null || annotationsFromIds.isEmpty()){
                    continue;
                }
                SolrAnnotation winnerAnnotation = annotationsFromIds.get(0); // highest quality

                //score calculation
                float sourceNumber = sources.size();
                float numOfDocs = ids.size();
                float topQuality = winnerAnnotation.getQuality();
                float normalizedFreq = 1.0f + (totalDocumentsFound > 0 ? (numOfDocs / totalDocumentsFound) : 0);
                float normalizedSolrScore = 1.0f + idToSolrScore.get(winnerAnnotation.getId())/maxScore;
                float score = (topQuality + sourceNumber) * normalizedFreq * normalizedSolrScore;

                SolrAnnotationSummary annotationSummary = new SolrAnnotationSummary(winnerAnnotation.getPropertyType(),
                        winnerAnnotation.getPropertyValue(),
                        winnerAnnotation.getSemanticTags(),
                        winnerAnnotation.getMongoid(),
                        winnerAnnotation.getSource(),
                        score);

                annotations.add(annotationSummary);

            }
        }

        return annotations;
    }

}
