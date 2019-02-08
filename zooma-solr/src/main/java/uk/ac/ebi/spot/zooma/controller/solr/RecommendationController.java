package uk.ac.ebi.spot.zooma.controller.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.model.solr.Recommendation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Simon Jupp
 * @date 27/11/2018
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RestController
@ExposesResourceFor(Recommendation.class)
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    @Qualifier("recommendationSolrTemplate")
    private SolrTemplate solrTemplate;

    @RequestMapping(value = "/search",  method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Recommendation>> recommend(
            @RequestParam(value = "propertyType", required = false) Collection<String> propertyTypes,
            @RequestParam(value = "propertyValue", required = false) Collection<String> propertyValues,
            @RequestParam(value = "target", required = true) String target,
            PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {


        SimpleQuery solrQuery = new SimpleQuery();

        Criteria queryCriteria = new Criteria("propertiesTypeTag").contains(target).or(new Criteria("propertiesValueTag").contains(target));

        if (propertyTypes != null) {
            String typesOr = String.join(" OR ", propertyTypes);
            queryCriteria = queryCriteria.connect().and(new Criteria("propertiesType").contains(typesOr));
        }

        if (propertyValues != null) {
            String valuesOr = String.join(" OR ", propertyValues);
            // queryCriteria.and(new Criteria("propertiesValue").contains(valuesOr));
            queryCriteria = queryCriteria.connect().and(new Criteria("propertiesValue").contains(valuesOr));
        }

        // queryCriteria.and("propertiesTypeTag").or("propertiesValuesTag").contains(target);

        // solrQuery.addCriteria(new Criteria("propertiesType").contains(typesOr).or("propertiesValue").contains(valuesOr).and("propertiesTypeTag").contains(target));

        solrQuery.addCriteria(queryCriteria);
        System.out.println(solrQuery.getCriteria().toString());

        Page<Recommendation> recommendationCollection=  solrTemplate.query(solrQuery, Recommendation.class);

        PagedResources<Recommendation> resources = assembler.toResource(recommendationCollection, linkTo(methodOn(RecommendationController.class).recommend(propertyTypes, propertyValues, target, assembler, pageable)).withSelfRel());
        return new ResponseEntity<>(assembler.toResource(recommendationCollection), HttpStatus.OK);
    }


    private static String COLON = ":";
    private static String QUOTUE = "\"";
    private static String SPACE = " ";
    private static String OR = "OR";
    private static String AND = "AND";

    private String createUnionQuery (String query, String ... fields) {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x< fields.length; x++) {
            builder.append(fields[x]);
            builder.append(COLON);
            builder.append(QUOTUE);
            builder.append(query);
            builder.append(QUOTUE);
            builder.append(SPACE);

            if (x+1 < fields.length) {
                builder.append(OR);
                builder.append(SPACE);

            }
        }
        return builder.toString();
    }

}
