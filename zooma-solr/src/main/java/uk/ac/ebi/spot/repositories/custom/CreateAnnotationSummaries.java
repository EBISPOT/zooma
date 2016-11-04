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
 * Created by olgavrou on 31/10/2016.
 */
@Repository
public class CreateAnnotationSummaries {

    @Autowired
    SolrTemplate solrTemplate;

    public List<AnnotationSummary> convertToAnnotationSumaries(List<SolrAnnotation> content, List<FacetPivotFieldEntry> pivots, long totalDocumentsFound, String annotatedPropertyValue){

        Map<String, Integer> propValueSourceNum = new HashMap<>();
        Map<String, List<String>> propValueIds = new HashMap<>();
        Map<String, String> propValueSemTags = new HashMap<>();
        Map<String, SolrAnnotation> propValueWinnerDoc = new HashMap<>();
        Map<String, Float> propValueQualityScore = new HashMap<>();
        float maxScore = 0.0f;
        Map<String, Float> idToScore = new HashMap<>();


        for (SolrAnnotation annotation : content){
            idToScore.put(annotation.getId(), annotation.getScore());
            if (annotation.getScore() > maxScore){
                maxScore = annotation.getScore();
            }
        }

        for (FacetPivotFieldEntry semanticTags : pivots){
            String semTag = semanticTags.getValue();
            List<FacetPivotFieldEntry> propertyValues = semanticTags.getPivot();
            for (FacetPivotFieldEntry propertyValue : propertyValues){
                String propValue = propertyValue.getValue(); //e.g. liver
                List<FacetPivotFieldEntry> sources = propertyValue.getPivot();

                propValueSourceNum.put(propValue, sources.size());
                propValueSemTags.put(propValue, semTag);

                List<String> ids = new ArrayList<>(); //ids are collected from both sources
                for (FacetPivotFieldEntry source : sources){
                    List<FacetPivotFieldEntry> sourceIds = source.getPivot();
                    for (FacetPivotFieldEntry sourceId : sourceIds){
                        ids.add(sourceId.getValue());
                    }
                }

                propValueIds.put(propValue, ids);
            }

        }

        for (String value : propValueIds.keySet()){
            List<String> ids = propValueIds.get(value);
            //create the query to get the biggest quality document and its quality score
            Query idQuery = new SimpleQuery();

            Criteria criteria1 = null;
            int idCount = 0;
            for (String id : ids){
                idCount++;
                if (criteria1 == null){
                    criteria1 = new Criteria("id").is(id);
                } else {
                    criteria1 = criteria1.or(new Criteria("id").is(id));
                }
                //search can't handle queries that are too long
                if (idCount > 100){
                    break;
                }
            }

            idQuery.addCriteria(criteria1);
            idQuery.setRows(1);
            idQuery.addSort(new Sort(Sort.Direction.DESC, "quality"));
            ScoredPage<SolrAnnotation> docs = solrTemplate.queryForPage(idQuery, SolrAnnotation.class);
            List<SolrAnnotation> annotations = docs.getContent();
            if (annotations != null && !annotations.isEmpty()){
                propValueWinnerDoc.put(value, annotations.get(0));
                propValueQualityScore.put(value, annotations.get(0).getQuality());
            }
        }


        //score calculation
        List<AnnotationSummary> annotations = new ArrayList<>();
        for (String value : propValueSemTags.keySet()){
            float sourceNumber = propValueSourceNum.get(value);
            float numOfDocs = propValueIds.get(value).size();
            float topQuality = propValueQualityScore.get(value);
            float normalizedFreq = 1.0f + (totalDocumentsFound > 0 ? (numOfDocs / totalDocumentsFound) : 0);
            float normalizedSolrScore = 1.0f + idToScore.get(propValueWinnerDoc.get(value).getId())/maxScore;
            float score = (topQuality + sourceNumber) * normalizedFreq * normalizedSolrScore;
            SolrAnnotation annotation = propValueWinnerDoc.get(value);

            SolrAnnotationSummary annotationSummary = new SolrAnnotationSummary(annotation.getPropertyType(),
                    annotation.getPropertyValue(),
                    annotation.getSemanticTags(),
                    annotation.getMongoid(),
                    annotation.getSource(),
                    score);

            annotations.add(annotationSummary);
        }

        return annotations;
    }

}
