package uk.ac.ebi.spot.zooma.service.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by olgavrou on 06/03/2017.
 */
@Service
public class AnnotationRepositoryServiceRead {


    private SolrTemplate solrTemplate;
    private String solrCore;

    @Autowired
    public AnnotationRepositoryServiceRead(SolrTemplate solrTemplate,
                                           @Value("${spring.data.solr.core}") String solrCore) {
        this.solrTemplate = solrTemplate;
        this.solrCore = solrCore;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Page<Annotation> findByPropertyValue(String propertyValue, Pageable pageable) throws IOException, SolrServerException {
        String query = propertyValueQuery(propertyValue);
        List<Annotation> results = query(query);
        return listToPageable(results, pageable);
    }

    public Page<Annotation> findByPropertyValue(String propertyValue, List<String> sources, Pageable pageable) throws IOException, SolrServerException {
        if (sources == null){
            return findByPropertyValue(propertyValue, pageable);
        }
        String query = andSourcesQuery(propertyValueQuery(propertyValue), sources);
        List<Annotation> results = query(query);
        return listToPageable(results, pageable);
    }

    public Page<Annotation> findByPropertyTypeAndPropertyValue(String propertyType, String propertyValue, Pageable pageable) throws IOException, SolrServerException {
        String query = andPropertyTypeQuery(propertyValueQuery(propertyValue), propertyType);
        List<Annotation> results = query(query);
        return listToPageable(results, pageable);
    }

    public Page<Annotation> findByPropertyTypeAndPropertyValue(String propertyType, String propertyValue, List<String> sources, Pageable pageable) throws IOException, SolrServerException {
        if (sources == null){
            return findByPropertyTypeAndPropertyValue(propertyType, propertyValue, pageable);
        }
        String query = andSourcesQuery(andPropertyTypeQuery(propertyValueQuery(propertyValue), propertyType), sources);
        List<Annotation> results = query(query);
        return listToPageable(results, pageable);
    }

    String propertyValueQuery(String propertyValue){
        StringJoiner or = new StringJoiner(" OR ");
        or.add("propertyValue:\"" + propertyValue + "\"");
        or.add("propertyValueStr:\"" + propertyValue + "\"^10");
        or.add("propertyValue:" + propertyValue);
        return or.toString();
    }

    String andPropertyTypeQuery(String query, String propertyType){
        StringJoiner and = new StringJoiner(" AND ");
        and.add(query);
        and.add("propertyType:\"" + propertyType + "\"");
        return and.toString();
    }

    String andSourcesQuery(String query, List<String> sources){
        StringJoiner and = new StringJoiner(" AND ");
        StringJoiner or = new StringJoiner(" OR ");
        and.add(query);
        for (String source : sources){
            or.add("source:\"" + source + "\"");
        }
        String sourcez = "(" + or.toString() + ")";
        and.add(sourcez);
        return and.toString();
    }

    private Page<Annotation> listToPageable(List<Annotation> results, Pageable pageable){
        int start = pageable.getOffset();
        int end = (start + pageable.getPageSize()) > results.size() ? results.size() : (start + pageable.getPageSize());
        Page<Annotation> pages = new PageImpl<Annotation>(results.subList(start, end), pageable, results.size());
        return pages;
    }

    private List<Annotation> query(String query) throws IOException, SolrServerException {
        //get the most resent document's date
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query.toString());
        solrQuery.setRows(1);
        solrQuery.addSort("lastModified", SolrQuery.ORDER.desc);
        solrQuery.setIncludeScore(true);
        QueryResponse response = solrTemplate.getSolrClient().query(this.solrCore, solrQuery);
        List<Annotation> results = response.getBeans(Annotation.class);
        LocalDateTime localDateTime = null;
        if (!results.isEmpty()){
            Date dateTime = results.get(0).getLastModified();
            localDateTime = dateTime.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        } else {
            return results;
        }

        //query again to boost by date
        SolrQuery q = new SolrQuery();
        if (localDateTime != null){
            q.set("bf", "div(1,sum(0.001,ms(" + localDateTime.toString() + "Z,lastModified)))^0.01");
//            q.set("bf", "sum(div(1,sum(0.001,ms(" + localDateTime.toString() + "Z,lastModified))),product(sum(votes,quality,sourceNum),0.001))^0.001");
        } //else {
//            q.set("bf","sum(votes,sourceNum,quality)^0.001");
//        }

//        q.set("boost","recip(ms(NOW,lastModified),3.16e-11,1,1)");

//        q.set("bf", "sum(product(recip(ms(NOW,lastModified),3.16e-11,1,1),1000),product(sum(votes,quality,sourceNum),0.001))");

//        q.set("boost", "sum(div(1,sum(0.001,ms(\"" + localDateTime.toString() + "Z\",lastModified))),product(sum(votes,quality,sourceNum),0.001))");

//        q.set("boost", "sum(div(1,sum(0.001,ms($qq,lastModified))),product(sum(votes,quality,sourceNum),0.001))");
//        q.set("qq","_query_:(propertyValue:\"cell migration\" AND propertyType:\"phenotype\" OR propertyValueStr:\"cell migration\"^10 OR propertyValue:cell migration&rows=1&sort=lastModified desc&fl=*,score)");

        q.set("q", query.toString());
        q.set("defType", "edismax");
        q.setRows(20);
        q.setIncludeScore(true);

        response = solrTemplate.getSolrClient().query(this.solrCore, q);
        results = response.getBeans(Annotation.class);

        if(results.isEmpty()){
            return results;
        }

        return results;
    }

}
