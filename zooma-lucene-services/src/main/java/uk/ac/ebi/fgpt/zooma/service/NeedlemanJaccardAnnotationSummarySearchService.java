package uk.ac.ebi.fgpt.zooma.service;

import java.io.IOException;
import java.util.ArrayList;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;



/**
 * This class extends AnnotationSummarySearchServiceDecorator and adds fuzzy string 
 * searching, in other words, functionality to find approximate matchings. 
 * Specifically, the metrics "Needleman-Wunsch"  and "Jaccard similarity" are included.
 * Implementations of simMetrics library are used. http://sourceforge.net/projects/simmetrics/ 

 * @author Jose Iglesias
 * @date 16/08/13         */

public class NeedlemanJaccardAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {
    
    private PropertyDAO propertyDAO;
    
    private Collection<String> propertyValueNormalized; 
    
    private NormalizerLexicalTechniques normalizer;
    
    private FilterLexicalTechniques filter;

    public NeedlemanJaccardAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
        
    }

    public Collection<String> getPropertyValueNormalized() {
        return propertyValueNormalized;
    }

    public void setPropertyValueNormalized(Collection<String> propertyValueNormalized) {
        this.propertyValueNormalized = propertyValueNormalized;
    }

    public NormalizerLexicalTechniques getNormalizer() {
        return normalizer;
    }

    public void setNormalizer(NormalizerLexicalTechniques normalizer) {
        this.normalizer = normalizer;
    }

    public FilterLexicalTechniques getFilter() {
        return filter;
    }

    public void setFilter(FilterLexicalTechniques filter) {
        this.filter = filter;
    }

    
    
    
    /* This method extends searchAndScore adding functionality to find approximate matchings.   */
    /*@Override
    public Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        getLog().debug("Calling searchAndScore... ");
        Map<AnnotationSummary, Float> results = super.searchAndScore(propertyValuePattern);

        if(!propertyValuePattern.contains(" and ")){
            
           // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
           Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

           for (String s : similarStrings.keySet()) {
               
               if(FilterLexicalTechniques.pass_filter_by_affirmative_negative(s,propertyValuePattern)){

                    getLog().debug("Calling searchAndScore... ");
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore(s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {

                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s);

                            getLog().debug("zoomaScore: " + zoomaScore + "   " + "ourScore: " + ourScore);

                            if (ourScore > zoomaScore) {
                                getLog().debug("Increasing score: " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                                results.put(as, ourScore);
                            }
                        }
                        else {
                            getLog().debug("Adding new AnnotationSummary. " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s));
                        }
                    }
               }
           }
        }
        
        getLog().debug("\nFinal scores: ");
        for (AnnotationSummary as : results.keySet()) {
            
            float FinalScore = results.get(as);
            getLog().debug("\t" + FinalScore);
        }
        
        return results;
    }*/
    
    /* This method extends searchAndScore adding functionality to find approximate matchings.   
       Very similar to searchAndScore, but the method searchAndScore_MaxScoreBooleanQuery calls 
       super.searchAndScore_MaxScoreBooleanQuery  */
    
    @Override
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyValuePattern) {

       
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        getLog().debug("Calling searchAndScore_MaxScoreBooleanQuery... ");
        

        Map<AnnotationSummary, Float> results = super.searchAndScore_QueryExpansion(propertyValuePattern);

        if(!propertyValuePattern.contains(" and ")){
        
            // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
           Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

           for (String s : similarStrings.keySet()) {
               
               if(FilterLexicalTechniques.pass_filter_by_affirmative_negative(s,propertyValuePattern)){

                    getLog().debug("Calling searchAndScore_MaxScoreBooleanQuery... ");
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore_QueryExpansion(s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {

                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            //More weight for the lexical score 
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            getLog().debug("zoomaScore: " + zoomaScore + "   " + "ourScore: " + ourScore);

                            if (ourScore > zoomaScore) {
                                //getLog().debug("Increasing score: " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                                results.put(as, ourScore);
                            }
                        }
                        else {
                            //getLog().debug("Adding new AnnotationSummary. " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                            //More weight for the lexical score 
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
                        }
                    }
               
               }
           }
        }
        
        getLog().debug("\nFinal scores: ");
        for (AnnotationSummary as : results.keySet()) {
            
            float FinalScore = results.get(as);
            getLog().debug("\t" + FinalScore);
        }
        
        return results;
        
    }
    
    
    
    /* This method extends searchAndScore adding functionality to find approximate matchings.   */
    /*@Override 
    public Map<AnnotationSummary, Float> searchAndScore(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        // original query results
        Map<AnnotationSummary, Float> results = super.searchAndScore(propertyType, propertyValuePattern);

        if(!propertyValuePattern.contains(" and ")){
            // use algorithm to find matching properties
            Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                
                if(FilterLexicalTechniques.pass_filter_by_affirmative_negative(s,propertyValuePattern)){
                
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore(propertyType, s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {

                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s);

                            if (ourScore > zoomaScore) {
                                results.put(as, ourScore);
                            }
                        }
                        else {
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s));
                        }
                    }
                }
            }
        }

        return results;
    }*/
    
    
    /* This method extends searchAndScore adding functionality to find approximate matchings.   */
    @Override 
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyType, String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        // original query results
        Map<AnnotationSummary, Float> results = super.searchAndScore_QueryExpansion(propertyType, propertyValuePattern);

        if(!propertyValuePattern.contains(" and ")){
            // use algorithm to find matching properties
            Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

            for (String s : similarStrings.keySet()) {

                if(FilterLexicalTechniques.pass_filter_by_affirmative_negative(s,propertyValuePattern)){
                
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore_QueryExpansion(propertyType, s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {

                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            if (ourScore > zoomaScore) {
                                results.put(as, ourScore);
                            }
                        }
                        else {
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
                        }
                    }
                }
            }
        }

        return results;
    }
    
    
    
    /*   "Needleman-Wunsch"  and "Jaccard similarity" are used to find approximateMatchings. */
    
    private Map<String, Float> findApproximateMatchings(String propertyValuePattern) {
        
        Map<String, Float> annotations =new HashMap();
        
        String property_value_normalized =  propertyValuePattern.toLowerCase() ;
        property_value_normalized = getNormalizer().removeStopWords( property_value_normalized );
        property_value_normalized = getNormalizer().removeCharacters( property_value_normalized );

        annotations = findNeedlemanMatchings(property_value_normalized, getPropertyValueNormalized(), 0.90f, 1, 0.0f );
        
        if(annotations.isEmpty()){
            annotations =  findJaccardMatchings(property_value_normalized, getPropertyValueNormalized(), 0.525f, 1, 0.999f );
        }
        
                
        return annotations;
    }
    
    
    

    
    
    /*   This methods finds matchings using "Needleman-Wunsch" distance. 
     *   Here, simmetrics library is used */
    private Map<String, Float> findNeedlemanMatchings(String property_value_normalized, Collection<String> propertyValueNormalized, float min_score, int num_max_annotations, float pct_cutoff ) {
        
        Map<String, Float> annotations = new HashMap();
        Map<String, Float> final_annotations = new HashMap();
        
        AbstractStringMetric metric = new NeedlemanWunch();        
                
        if(propertyValueNormalized!=null && !propertyValueNormalized.isEmpty()){        
            
            Iterator iterator = propertyValueNormalized.iterator();

            while (iterator.hasNext()) { 

                String property_dictionary = (String) iterator.next();
                 
                float result = metric.getSimilarity(property_value_normalized, property_dictionary);
                
                if(result>=min_score){
                    annotations.put(property_dictionary, result);
                }
            }
        }
        
        if(annotations.size()>=1){
            
            final_annotations = getFilter().filterAnnotations(annotations,min_score,num_max_annotations,pct_cutoff);
        }

        return final_annotations;
    }
    
    
    
    
        
        
    /*   This methods finds matchings using "Jaccard" similarity. 
     *   Here, simmetrics library is used */
    
    private Map<String, Float> findJaccardMatchings(String property_value_normalized, Collection<String> propertyValueNormalized, float min_score, int num_max_annotations, float pct_cutoff) {
            
    
        Map<String, Float> annotations = new HashMap();
        Map<String, Float> final_annotations = new HashMap();
        
        AbstractStringMetric metric = new JaccardSimilarity();        
                
        if(propertyValueNormalized!=null && !propertyValueNormalized.isEmpty()){        
            
            Iterator iterator = propertyValueNormalized.iterator();

            while (iterator.hasNext()) { 

                String property_dictionary = (String) iterator.next();
                
                float result = metric.getSimilarity(property_value_normalized, property_dictionary);
                
                if(result>=min_score){
                    annotations.put(property_dictionary, result);
                }
            }
        }
        
        if(annotations.size()>=1){
            
            final_annotations = getFilter().filterAnnotations(annotations,min_score,num_max_annotations,pct_cutoff);
        }
        
        return final_annotations;
    }
    
    
    
    
   /* A sparql query (via PropertyDAO) is used to extract all properties. Then the strings are normalized  */
      public void doInitialization() throws IOException {
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        
        
        
        // get all properties 
        Collection<Property> properties = getPropertyDAO().read();
        

        propertyValueNormalized = new ArrayList();
        
        Iterator iterator =properties.iterator();
        
        while (iterator.hasNext()) {
            
            Property p = (Property) iterator.next();
            
            String property_value =  p.getPropertyValue().toLowerCase() ;
            
            property_value = getNormalizer().removeStopWords( property_value );
            property_value = getNormalizer().removeCharacters( property_value );
            
            
            propertyValueNormalized.add(property_value);
        }
        
        time_end = System.currentTimeMillis();
        
        System.out.println("Load all properties has taken "+ ( time_end - time_start ) +" milliseconds");
        System.out.println(propertyValueNormalized.size() + " properties read using SPARQL");
        

    }

    
}