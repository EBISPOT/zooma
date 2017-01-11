package uk.ac.ebi.spot.zooma.repository.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
import uk.ac.ebi.spot.zooma.utils.CriteriaGenerator;

import java.util.*;

/**
 * This class implements the methods of the Custom repository. It uses the {@link SolrTemplate} to query and return
 * different views of the {@link Annotation} document.
 *
 * Created by olgavrou on 14/10/2016.
 */
public class SolrAnnotationRepositoryImpl implements CustomSolrAnnotationRepository {

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    CreateAnnotationSummariesRepository createAnnotationSummariesRepository;

    /**
     * Defines the criteria based on the propertyValue and then performs a search
     *
     * @param propertyValue the term that will be searched for
     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    @Override
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue) {

        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyValue", propertyValue);

        return  findAnnotationSummariesByCriteria(criteriaMap, null);
    }

    @Override
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue, List<String> sourceNames) {

        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyValue", propertyValue);


        return  findAnnotationSummariesByCriteria(criteriaMap, sourceNames);
    }

    /**
     * Defines the criteria based on the propertyValue and propertyType and then performs a search
     *
     * @param propertyType the type of the term that will be searched for,
     *                     and that that can help boost the score of the results
     * @param propertyValue the term that will be searched for

     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    @Override
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyType", propertyType);
        criteriaMap.put("propertyValue", propertyValue);
        return findAnnotationSummariesByCriteria(criteriaMap, null);
    }

    @Override
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue, List<String> sourceNames) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyType", propertyType);
        criteriaMap.put("propertyValue", propertyValue);
        return findAnnotationSummariesByCriteria(criteriaMap, sourceNames);
    }

    /**
     * Uses solr pivoting to search for a term value in the {@link Annotation} documents stored in
     * the Solr server.
     * The results are pivoted, so essentially grouped, by semanticTag, then by propertyValueStr (a String copy of the indexed propertyValue
     * so the pivot group won't break on the different value words), then by source and lastly by id.
     * The result will be seperate semanticTag, each will contain a group of their different propertyValueStr, and again each propertyValueStr
     * will contain a group of the different sources and again, each source will contain the Document ids.
     *
     * The information is passed to the {@link CreateAnnotationSummariesRepository} repository, that will convert the {@link Annotation}
     * documents into {@link AnnotationSummary}s.
     *
     * @param criteriaMap the map from which the criteria will be made for solr will query, e.g.: <"propertyValue", "<a property value>">
     *                    can result to criteria: where("propertyValue").is("a property value")
     * @param sourceNames list of the sources we want to restrict the search to, NULL when none specified
     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    private List<AnnotationSummary> findAnnotationSummariesByCriteria(Map<String, String> criteriaMap, List<String> sourceNames){

        FacetQuery query = new SimpleFacetQuery();
        Criteria criteria = CriteriaGenerator.makeStrictCriteria(criteriaMap);

        query.addCriteria(criteria);

        if (sourceNames != null && !sourceNames.isEmpty()) {
            Criteria sourceCriteria = CriteriaGenerator.makeDatasourceCriteria(sourceNames);
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
            if (totalElements == 0){
                return  new ArrayList<>();
            }
        }

        query.setRows((int) totalElements);

        //facet.pivot=semanticTag,annotatedPropertyValue,source,id&facet=true
        FacetOptions facetOptions = new FacetOptions();
        PivotField pivotField = new SimplePivotField("propertyValueStr", "semanticTag", "source", "id");
        facetOptions.addFacetOnPivot("propertyValueStr", "semanticTag", "source", "id");
        facetOptions.setFacetSort(FacetOptions.FacetSort.INDEX);
        facetOptions.setFacetLimit((int) totalElements);

        query.setFacetOptions(facetOptions);

        FacetPage<Annotation> facetPage = solrTemplate.queryForFacetPage(query, Annotation.class);
        List<FacetPivotFieldEntry> pivots = facetPage.getPivot(pivotField);
        List<Annotation> content = facetPage.getContent();
        long totalDocumentsFound = facetPage.getTotalElements();

        return createAnnotationSummariesRepository.convertToAnnotationSumaries(content, pivots, totalDocumentsFound);
    }

}
