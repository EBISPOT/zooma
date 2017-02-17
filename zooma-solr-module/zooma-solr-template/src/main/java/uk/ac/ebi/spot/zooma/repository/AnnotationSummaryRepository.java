package uk.ac.ebi.spot.zooma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.FacetPivotFieldEntry;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.utils.CriteriaGenerator;

import java.util.*;

/**
 * Created by olgavrou on 17/02/2017.
 */
@Repository
public class AnnotationSummaryRepository {

    @Autowired
    SolrTemplate solrTemplate;


    public List<AnnotationSummary>  findAnnotationSummariesByCriteria(Map<String, String> criteriaMap, List<String> desiredSources) {

        SimplePivotField pivotField = new SimplePivotField("propertyValueStr", "semanticTag", "source", "quality", "id");

        FacetPage<Annotation> facetPage = getFacetPage(criteriaMap, pivotField, desiredSources);

        if(facetPage.getTotalElements() == 0){
            return new ArrayList<>();
        }

        List<AnnotationSummary> annotations = getAnnotationSummariesFromSolrFacetPage(facetPage, pivotField);

        return annotations;

    }

    private FacetPage<Annotation> getFacetPage(Map<String, String> criteriaMap, SimplePivotField pivotField, List<String> desiredSources) {
        FacetQuery query = new SimpleFacetQuery();

        Criteria criteria = CriteriaGenerator.makeStrictCriteria(criteriaMap);

        query.addCriteria(criteria);

        if (desiredSources != null && !desiredSources.isEmpty()) {
            Criteria sourceCriteria = CriteriaGenerator.makeDatasourceCriteria(desiredSources);
            query.addCriteria(sourceCriteria);
        }

        //getnumber of elements in query to query again
        query.setRows(0);
        ScoredPage<Annotation> page = solrTemplate.queryForPage(query, Annotation.class);
        long totalElements = page.getTotalElements();

        //if can't find with strict criteria
        if (totalElements == 0){
            query = new SimpleFacetQuery();
            criteria = CriteriaGenerator.makeFlexibleCriteria(criteriaMap);
            query.addCriteria(criteria);
            query.setRows(0);
            page = solrTemplate.queryForPage(query, Annotation.class);
            totalElements = page.getTotalElements();
        }

        query.setRows((int) totalElements);

        FacetOptions facetOptions = new FacetOptions();

        facetOptions.addFacetOnPivot(pivotField.toString().split(","));
        facetOptions.setFacetSort(FacetOptions.FacetSort.INDEX);
        facetOptions.setFacetLimit((int) totalElements);

        query.setFacetOptions(facetOptions);

        return solrTemplate.queryForFacetPage(query, Annotation.class);
    }


    private List<AnnotationSummary> getAnnotationSummariesFromSolrFacetPage(FacetPage<Annotation> facetPage, SimplePivotField pivotField) {

        List<FacetPivotFieldEntry> pivots = facetPage.getPivot(pivotField);
        List<Annotation> content = facetPage.getContent();
        long totalDocumentsFound = facetPage.getTotalElements();
        float maxSolrScore = 0.0f; // max solr score of all annotations
        List<AnnotationSummary> annotations = new ArrayList<>();

        //map the ids to their information in order to not query
        //solr again for it
        Map<String, Float> idToSolrScore = new HashMap<>();
        Map<String, String> idToPropType = new HashMap<>();
        Map<String, String> idToMongoId = new HashMap<>();
        Map<String, Collection<String>> idToSemTags = new HashMap<>();

        for (Annotation annotation : content){
            idToSolrScore.put(annotation.getId(), annotation.getScore());
            idToPropType.put(annotation.getId(), annotation.getPropertyType());
            idToMongoId.put(annotation.getId(), annotation.getMongoid());
            idToSemTags.put(annotation.getId(), annotation.getSemanticTag());
            if (annotation.getScore() > maxSolrScore){
                maxSolrScore = annotation.getScore();
            }
        }

        for (FacetPivotFieldEntry propertyValues : pivots){
            List<FacetPivotFieldEntry> semanticTags = propertyValues.getPivot();
            for (FacetPivotFieldEntry semanticTag : semanticTags){

                //for this semantic tag we will construct an annotation summary
                String winnerAnnotationId = ""; // highest quality annotation
                ArrayList<String> foundSources = new ArrayList<>();
                float maxQuality = 0.0f;
                int numOfDocsInPivot = 0;

                List<FacetPivotFieldEntry> sources = semanticTag.getPivot();
                for (FacetPivotFieldEntry source : sources){
                    foundSources.add(source.getValue()); // keep all the sources
                    List<FacetPivotFieldEntry> qualityScores = source.getPivot();

                    for (FacetPivotFieldEntry quality : qualityScores){
                        if (Float.valueOf(quality.getValue()) > maxQuality){
                            maxQuality = Float.valueOf(quality.getValue());
                            numOfDocsInPivot = numOfDocsInPivot + quality.getPivot().size();
                            winnerAnnotationId = quality.getPivot().get(0).getValue(); // from the ids that have the highest quality, pick one
                        }
                    }
                }

                //score calculation
                float score = caluclateScore(sources.size(), maxQuality, numOfDocsInPivot, totalDocumentsFound, idToSolrScore.get(winnerAnnotationId), maxSolrScore);

                //construct the annotation summary
                AnnotationSummary annotationSummary = new AnnotationSummary(idToPropType.get(winnerAnnotationId),
                        propertyValues.getValue(),
                        idToSemTags.get(winnerAnnotationId),
                        idToMongoId.get(winnerAnnotationId),
                        foundSources,
                        score);

                annotations.add(annotationSummary);

            }
        }
        return annotations;
    }

    private float caluclateScore(int sourceSize, float maxQuality, int numOfDocsInPivot, long totalDocumentsFound, Float documentSolrScore, float maxScore) {
        float normalizedFreq = 1.0f + (totalDocumentsFound > 0 ? (numOfDocsInPivot / totalDocumentsFound) : 0);
        float normalizedSolrScore = 1.0f + (maxScore > 0 ? (documentSolrScore/maxScore) : 0);
        return  (maxQuality + sourceSize + normalizedSolrScore) * normalizedFreq ;
    }


}
