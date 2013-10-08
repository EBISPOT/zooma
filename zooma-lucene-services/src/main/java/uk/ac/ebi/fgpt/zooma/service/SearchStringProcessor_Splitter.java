

package uk.ac.ebi.fgpt.zooma.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

/**
 *
 * @author Jose
 */
public class SearchStringProcessor_Splitter implements SearchStringProcessor{


    @Override
    public float getBoostFactor() {
        return 0.7f;
    }

    
    /**
    * Returns true if the property value contains exactly one " and ". 
    * Returns false otherwise.
    * 
    * @author Jose Iglesias
    * @date 12/08/13
    */
    @Override
    public boolean canProcess(String searchString, String type) {
        
        if( StringUtils.countMatches(searchString, " and ")==1 ){
            return true;
        }else{
            return false; 
        }
    }

    
    
    /**
    * Splits a string using the keyword "and"
    * 
    * @author Jose Iglesias
    * @date 12/08/13
    */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        
        ArrayList<String> processedStrings = new ArrayList<String>();
        
        String[] expressions = searchString.split(" and ");
        
        if(expressions!=null && expressions.length==2){

            for(int i=0; i<expressions.length ; i++){
                processedStrings.add(expressions[i]);
            }
        }

        return processedStrings;  
    }

    

}