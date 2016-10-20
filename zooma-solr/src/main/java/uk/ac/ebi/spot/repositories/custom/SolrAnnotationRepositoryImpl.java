package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.util.ZoomaUtils;
import uk.ac.ebi.spot.utils.AbstractStringQualityBasedScorer;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by olgavrou on 14/10/2016.
 */
public class SolrAnnotationRepositoryImpl implements CustomSolrAnnotationRepository {

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    AbstractStringQualityBasedScorer abstractStringQualityBasedScorer;

    private float max;
    private float min;

    @Override
    public List<SolrAnnotation> findByAnnotatedPropertyValueGroupBySemanticTags(String annotatedPropertyValue) {

        Date date = new Date();
        Timestamp timestamp1 = new Timestamp(date.getTime());

        FacetQuery query = new SimpleFacetQuery();
        Criteria criteria = new Criteria("annotatedPropertyValue").is(annotatedPropertyValue);

        query.addCriteria(criteria);
        //getnumber of elements in query to query again
        query.setRows(0);
        ScoredPage<SolrAnnotation> page = solrTemplate.queryForPage(query, SolrAnnotation.class);
        long totalElements = page.getTotalElements();
        if (totalElements == 0){
            query = new SimpleFacetQuery();
            criteria = Criteria.where("annotatedPropertyValue").expression(annotatedPropertyValue);
            query.addCriteria(criteria);
            query.setRows(0);
            page = solrTemplate.queryForPage(query, SolrAnnotation.class);
            totalElements = page.getTotalElements();
        }

        query.setRows((int) totalElements);

        //facet.pivot=semanticTags,annotatedPropertyValue,source,id&facet=true
        FacetOptions facetOptions = new FacetOptions();
        PivotField pivotField = new SimplePivotField("semanticTags", "annotatedPropertyValueStr", "source", "id");
        facetOptions.addFacetOnPivot("semanticTags", "annotatedPropertyValueStr", "source", "id");
        facetOptions.setFacetSort(FacetOptions.FacetSort.INDEX);
        facetOptions.setFacetLimit(1000);

        query.setFacetOptions(facetOptions);

        FacetPage<SolrAnnotation> facetPage = solrTemplate.queryForFacetPage(query, SolrAnnotation.class);

        List<FacetPivotFieldEntry> pivots = facetPage.getPivot(pivotField);

        Map<String, Integer> propValueSourceNum = new HashMap<>();
        Map<String, List<String>> propValueIds = new HashMap<>();
        Map<String, String> propValueSemTags = new HashMap<>();
        Map<String, SolrAnnotation> propValueWinnerDoc = new HashMap<>();
        Map<String, Float> propValueQualityScore = new HashMap<>();
        float maxScore = 0.0f;
        Map<String, Float> idToScore = new HashMap<>();


        List<SolrAnnotation> content = facetPage.getContent();


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
        List<SolrAnnotation> annotations = new ArrayList<>();
        for (String value : propValueSemTags.keySet()){
            float sourceNumber = propValueSourceNum.get(value);
            float numOfDocs = propValueIds.get(value).size();
            float topQuality = propValueQualityScore.get(value);
            int annotationCount = 46246;
            float normalizedFreq = 1.0f + (annotationCount > 0 ? (numOfDocs / annotationCount) : 0);
            float score = (topQuality + sourceNumber) * normalizedFreq + idToScore.get(propValueWinnerDoc.get(value).getId())/maxScore;
            SolrAnnotation annotation = propValueWinnerDoc.get(value);
            annotation.setQuality(score);
            annotations.add(annotation);
        }


        float maxScoreForNormalization = 0.0f;
        SolrAnnotation maxAnnotation = null;

        Map<AnnotationSummary, Float> annotationsToScore = abstractStringQualityBasedScorer.score(annotations, annotatedPropertyValue);

        //cutoff scores based on the difference between the first score
        max = Collections.max(annotationsToScore.values());
        min = Collections.min(annotationsToScore.values());
        if (max == min){
            min = 0.0f;
        }

        //replace the quality with the newly calculated score
        for (AnnotationSummary annotationS : annotationsToScore.keySet()){
            SolrAnnotation annotation = (SolrAnnotation) annotationS;
            float normScore = normaliseScore(annotationsToScore.get(annotation));
            annotation.setQuality(normScore);
            annotationsToScore.put(annotation, normScore);
        }


        List<AnnotationSummary> results = ZoomaUtils.filterAnnotationSummaries(annotationsToScore, 80f, 0.9f);

        //Make sure the results are sorted (highest score first).
        Collections.sort(results, new Comparator<AnnotationSummary>() {
            @Override public int compare(AnnotationSummary o1, AnnotationSummary o2) {
                return annotationsToScore.get(o2).compareTo(annotationsToScore.get(o1));
            }
        });


        for (AnnotationSummary annotation : results){
            System.out.println("============================");
            System.out.println("Value: " + annotation.getAnnotatedPropertyValue());
            System.out.println("Type: " + annotation.getAnnotatedPropertyType());
            System.out.println("SemanticTag: " + annotation.getSemanticTags());
            System.out.println("Score: " + annotation.getQuality());
            System.out.println("============================");
        }
        Date date1 = new Date();
        Timestamp timestamp2 = new Timestamp(date1.getTime());
        System.out.println("----" + timestamp1);
        System.out.println("----" + timestamp2);
        System.exit(0);
        return null;
    }

    private float normaliseScore(float score){
        if ((score - min) < 0) {
            return 50;
        }
        else {
            return 50 + (50 * (score - min) /
                    (max - min));
        }
    }

}
