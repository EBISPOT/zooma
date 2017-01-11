package uk.ac.ebi.spot.zooma.utils;

import org.springframework.data.solr.core.query.Criteria;

import java.util.List;
import java.util.Map;

/**
 * Created by olgavrou on 03/01/2017.
 */
public class CriteriaGenerator {

    public static Criteria makeStrictCriteria(Map<String, String> criteriaMap){
        Criteria criteria = null;
        for (String key : criteriaMap.keySet()){
            if (criteria == null){
                criteria = new Criteria(key).is(criteriaMap.get(key));
            } else {
                criteria = criteria.and(new Criteria(key).is(criteriaMap.get(key)));
            }
        }

        return criteria;
    }

    public static Criteria makeFlexibleCriteria(Map<String, String> criteriaMap){
        Criteria criteria = null;
        for (String key : criteriaMap.keySet()){
            if (criteria == null){
                criteria = Criteria.where(key).expression(criteriaMap.get(key));
            } else {
                criteria = criteria.and(Criteria.where(key).expression(criteriaMap.get(key)));
            }
        }

        return criteria;
    }

    public static Criteria makeDatasourceCriteria(List<String> sourceNames){
        Criteria criteria = null;
        for (String name : sourceNames){
            if (criteria == null){
                criteria = new Criteria("source").is(name);
            } else {
                criteria = criteria.or(new Criteria("source").is(name));
            }
        }
        return criteria;
    }

}
