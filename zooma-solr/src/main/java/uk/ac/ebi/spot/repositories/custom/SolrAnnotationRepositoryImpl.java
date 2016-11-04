package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SolrAnnotation;

import java.util.*;

/**
 * Created by olgavrou on 14/10/2016.
 */
public class SolrAnnotationRepositoryImpl implements CustomSolrAnnotationRepository {

    @Autowired
    SolrTemplate solrTemplate;

    @Autowired
    CreateAnnotationSummaries createAnnotationSummaries;

    @Override
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue) {

        FacetQuery query = new SimpleFacetQuery();
        Criteria criteria = new Criteria("propertyValue").is(propertyValue);

        query.addCriteria(criteria);
        //getnumber of elements in query to query again
        query.setRows(0);
        ScoredPage<SolrAnnotation> page = solrTemplate.queryForPage(query, SolrAnnotation.class);
        long totalElements = page.getTotalElements();
        if (totalElements == 0){
            query = new SimpleFacetQuery();
            criteria = Criteria.where("propertyValue").expression(propertyValue);
            query.addCriteria(criteria);
            query.setRows(0);
            page = solrTemplate.queryForPage(query, SolrAnnotation.class);
            totalElements = page.getTotalElements();
            if (totalElements == 0){
                return  new ArrayList<>();
            }
        }

        query.setRows((int) totalElements);

        //facet.pivot=semanticTags,annotatedPropertyValue,source,id&facet=true
        FacetOptions facetOptions = new FacetOptions();
        PivotField pivotField = new SimplePivotField("semanticTags", "propertyValueStr", "source", "id");
        facetOptions.addFacetOnPivot("semanticTags", "propertyValueStr", "source", "id");
        facetOptions.setFacetSort(FacetOptions.FacetSort.INDEX);
        facetOptions.setFacetLimit((int) totalElements);

        query.setFacetOptions(facetOptions);

        FacetPage<SolrAnnotation> facetPage = solrTemplate.queryForFacetPage(query, SolrAnnotation.class);

        List<FacetPivotFieldEntry> pivots = facetPage.getPivot(pivotField);

        List<SolrAnnotation> content = facetPage.getContent();

        long totalDocumentsFound = facetPage.getTotalElements();

        return createAnnotationSummaries.convertToAnnotationSumaries(content, pivots, totalDocumentsFound, propertyValue);
    }

}
