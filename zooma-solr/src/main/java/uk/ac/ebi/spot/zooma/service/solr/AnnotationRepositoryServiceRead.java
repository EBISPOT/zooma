package uk.ac.ebi.spot.zooma.service.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.utils.Scorer;

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

    Scorer<Annotation> scorer;

    @Autowired
    public AnnotationRepositoryServiceRead(SolrTemplate solrTemplate,
                                           Scorer<Annotation> scorer) {
        this.solrTemplate = solrTemplate;
        this.scorer = scorer;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
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

        //get the most resent document's date
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

        //query again to boost by date
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

        float maxSolrScore = results.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore())).get().getScore();

        results.stream().forEach(annotation -> annotation.setQuality(annotation.getScore()));

        Map<Annotation, Float> annotationsToScore = scorer.score(results, propertyValue);

        List<Annotation> finalResult = new ArrayList<>();
        finalResult.addAll(annotationsToScore.keySet());
        finalResult.stream().forEach(annotation -> annotation.setQuality(normScore(maxSolrScore, annotationsToScore.get(annotation))));
        return finalResult;
    }

    // convert to 100 where 100 is the max solr score compared to the score they get after the similarity algorithm
    private float normScore(Float maxSolrScoreBeforeScorer, Float scoreAfterScorer) {
        float dx = 100 * ((maxSolrScoreBeforeScorer - scoreAfterScorer) / maxSolrScoreBeforeScorer);
        float n = 50 + (50 * (100 - dx) / 100);
        return n;
    }

}
