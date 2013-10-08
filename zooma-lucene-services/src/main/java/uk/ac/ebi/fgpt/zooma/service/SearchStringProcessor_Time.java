

package uk.ac.ebi.fgpt.zooma.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

/** 
 * This class handles the processing of properties of type "time".   
 * @author Jose Iglesias
 * @date 16/08/13         
 */
public class SearchStringProcessor_Time implements SearchStringProcessor{


    @Override
    public float getBoostFactor() {
        return 0.95f;
    }


    /**
    * Returns true if the property type is equal to time,age, ect.. 
    * Returns false otherwise.
    */
    @Override
    public boolean canProcess(String searchString,String type) {
        

        if(type!=null && !type.isEmpty()){
            
            type = type.toLowerCase();
        
            if( type.contentEquals("time")              ||     type.contentEquals("time unit")  || 
                type.contentEquals("time_unit")         ||     type.contentEquals("timeunit")   || 
                type.contentEquals("age")               ||     type.contentEquals("derived time unit") || 
                type.contentEquals("time derived unit") ||     type.contentEquals("period") ){

                return true;
            }
        }

        
        return false; 
    }

    
    /**
    * Takes a string, looks for numbers (such as: int,floats and intervals) in the string, 
    * removes them and returns the processed string.
    */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        
        ArrayList<String> processedStrings = new ArrayList<String>();
        
        String processedString="";

        String space = "\\s{0,2}";

        //pattern for number: int or float..
        String number_float = space +  "\\d{1,10}.\\d{1,10}" + space; 
        String number_int = space + "\\d{1,10}" + space; 

        //pattern for interval (e.g: 3-4 days).. 
        String interval_float =  "((" +number_float  + "-"  + number_float + ")|(" + number_float  + "to"  + number_float + "))";
        String interval_int =  "((" +number_int  + "-"  + number_int + ")|(" + number_int  + "to"  + number_int + "))";

        Pattern pattern_interval_float  =   Pattern.compile(interval_float);
        Pattern pattern_interval_int    =   Pattern.compile(interval_int);
        Pattern pattern_number_float    =   Pattern.compile(number_float);
        Pattern pattern_number_int      =   Pattern.compile(number_int);
        
        Matcher matcher_interval_float  =   pattern_interval_float.matcher(searchString);
        Matcher matcher_interval_int    =   pattern_interval_int.matcher(searchString);
        Matcher matcher_number_float    =   pattern_number_float.matcher(searchString);
        Matcher matcher_number_int      =   pattern_number_int.matcher(searchString);

        String substring_number=null;
        
        if( matcher_interval_float!=null && matcher_interval_float.find() ){

            substring_number = matcher_interval_float.group();
   
        }else if( matcher_interval_int!=null && matcher_interval_int.find() ){

            substring_number = matcher_interval_int.group();
 
        }else if( matcher_number_float!=null && matcher_number_float.find() ){

            substring_number = matcher_number_float.group();

        }else if( matcher_number_int!=null && matcher_number_int.find() ){

            substring_number = matcher_number_int.group();
        }
        
        if(substring_number!=null){
            processedString = searchString.replaceAll( substring_number, " ");
        
            //Manage and remove extra spaces:
            if (processedString.contains("   "))
                processedString = processedString.replace("   ", " ");

            if (processedString.contains("  "))
                processedString = processedString.replace("  ", " ");

            if (processedString.endsWith(" "))
                processedString = processedString.substring(0, processedString.length()-1);

            if (processedString.startsWith(" "))
                processedString = processedString.substring(1, processedString.length());
        
        }
        
        if( ! processedString.isEmpty())
              processedStrings.add(processedString);
        
        return processedStrings;  
    }

}