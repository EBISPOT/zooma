

package uk.ac.ebi.fgpt.zooma.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;


/** 
 * This class handles the processing of properties of type "compound".   
 * @author Jose Iglesias
 * @date 16/08/13         */

public class SearchStringProcessor_Compounds implements SearchStringProcessor{

    ArrayList<String> units;  // units contains all subclasses of "concentration unit" (UO_0000051). 


    @Override
    public float getBoostFactor() {
        return 0.95f;
    }
   
    /**
    * Returns true if the property type is equal to compound or growth_condition. 
    * Returns false otherwise.                  */
    @Override
    public boolean canProcess(String searchString, String type) {
        
        if(type!=null && !type.isEmpty()){
            
            type = type.toLowerCase();
            
            if(  type.contentEquals("compound")          || type.contentEquals("compounds") ||
                 type.contentEquals("growth condition")  || type.contentEquals("growth_condition")    ){
                
                 return true;
            }
        }
        
        return false; 
    }


    /**
    * Takes a string, looks for numbers and concentration units in the string, 
    * removes them and returns the processed strings. Normally, one string is returned.   
    */
    @Override   
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        
        ArrayList<String> processedStrings = new ArrayList<String>();

        String processedString=searchString.toLowerCase();

        //pattern for number: int or float..
        String space = "\\s{1}";      //space is important in order not to remove numbers within compounds.. (e.g indole-3-acetic acid)
        String number_float =    "\\d{1,10}.\\d{1,10}" + space; 
        String number_int =   "\\d{1,10}" + space; 

        Pattern pattern_number_float    =   Pattern.compile(number_float);
        Pattern pattern_number_int      =   Pattern.compile(number_int);
        
        Matcher matcher_number_float    =   pattern_number_float.matcher(searchString);
        Matcher matcher_number_int      =   pattern_number_int.matcher(searchString);

        String substring_number=null;
        
        if( matcher_number_float!=null && matcher_number_float.find() ){

            substring_number = matcher_number_float.group();

        }else if( matcher_number_int!=null && matcher_number_int.find() ){

            substring_number = matcher_number_int.group();
        }
        
        //Remove detected number
        if(substring_number!=null){
            processedString = searchString.replaceAll( substring_number, " ");
        }
        
        
        boolean removed_unit = false;
        
        //Remove concentration unit
        for(String unit: getUnits()){
            
            if( processedString.contains(" " + unit + " ") || processedString.startsWith(unit + " ") || processedString.endsWith(" " + unit) ){
                
                processedString = processedString.replaceAll( unit, " ");
                removed_unit = true;
            }        
        }
        
        
        
        //Sometimes units within compounds are in plural (e.g: metformin 50 milligrams per kilogram)
        //So, a more flexible/approximate mapping for units is included

        if( !removed_unit){

            ArrayList<String> substrings = extractSubstrings(processedString);
            
            for (String substring: substrings){

                if( approximateMatching_Unit (substring, getUnits() ) ){
                    
                    processedString = processedString.replaceAll( substring, " ");
                    break;
                }
            }
  
        }

        //Remove unnecesary spaces..
        if (processedString.contains("    "))
            processedString = processedString.replace("    ", " ");
        
        if (processedString.contains("   "))
            processedString = processedString.replace("   ", " ");

        if (processedString.contains("  "))
            processedString = processedString.replace("  ", " ");

        if (processedString.endsWith(" "))
            processedString = processedString.substring(0, processedString.length()-1);

        if (processedString.startsWith(" "))
            processedString = processedString.substring(1, processedString.length());

        // If processedString is not equal to the original string...
        if( ! processedString.contentEquals(searchString.toLowerCase()) )
            processedStrings.add(processedString);
        
        return processedStrings;  
    }

    
    
   /**
    * Extract substrings of the original property.
    * These strings potentially contain the concentration unit
    */
    private ArrayList<String> extractSubstrings(String processedString) {
        
        
        String words[] = StringUtils.split(processedString);
        
        ArrayList<String> possibleUnits = new ArrayList<String>();
        ArrayList<String> possibleUnits_notEmpty = new ArrayList<String>();

        if( words.length>1 ){
            
            possibleUnits.add( StringUtils.join(words, " ", 1,words.length) );
            possibleUnits.add( StringUtils.join(words, " ", 2,words.length) );
            possibleUnits.add( StringUtils.join(words, " ", 3,words.length) );
            possibleUnits.add( StringUtils.join(words, " ", 0,words.length-1) );
            possibleUnits.add( StringUtils.join(words, " ", 0,words.length-2) );
            possibleUnits.add( StringUtils.join(words, " ", 0,words.length-3) );
        }
        
        for(String s:possibleUnits){
            
            if(!s.isEmpty())
                possibleUnits_notEmpty.add(s); 
        }
        
        return possibleUnits_notEmpty;
    }

    
    
    /**
    * Check if substring matches approximately to any concentration unit 
    * The method uses LevenshteinDistance. Only 1 edition/change between strings 
    * is permissible to consider that there is an approximate matching.
    */
    private boolean approximateMatching_Unit(String substring, ArrayList<String> units) {
        
        for(String unit:units){
            
            if(unit.length()>2){  //Exclude abbreviations/acronyms units
                
                if( StringUtils.getLevenshteinDistance(substring, unit, 1) != -1 ){
                    return true;
                }
            }
        }
        
        return false;      
    }


    /* Initializes "units" from a file containing all subclasses of "concentration unit" (UO_0000051).  */
    public void init() throws IOException {
        
      units = new ArrayList();


        try {

            InputStream bufferFile = this.getClass().getClassLoader().getResourceAsStream("efo_dictionary_concentrationUnit.txt");
            
            if(bufferFile!=null){

               String stringFile = inputStream_To_String(bufferFile,4000);

               if(stringFile!=null && !stringFile.isEmpty()){

                   String line;

                   String[] lines = stringFile.split("\n");

                   for(int i=0; i<lines.length ; i++){

                      line = lines[i];
                      String[] fields = line.split("\t");  

                      if(fields.length>2){
                           units.add(fields[0]);
                      }
                   }
               }
            }
            
            System.out.println(units.size() + " concentration units read from file");

      }catch(Exception e){
         e.printStackTrace();
         
      }
   
    }
    
    
    
    private String inputStream_To_String(InputStream is, int tam) throws Exception {
      

        String s=""; 

        try{

           byte[] buffer  = new byte[tam];

           int i;

           while((i=is.read(buffer, 0, tam))!=-1){ 
               s = s.concat(new String(buffer));
           }

        }catch(Exception e){

           // if any I/O error occurs
           e.printStackTrace();

           return null;
        }finally{

           // releases system resources associated with this stream
           if(is!=null)
              is.close();

           return s;
        }
   }
    
    
   public ArrayList<String> getUnits() {
        return units;
   }

   public void setUnits(ArrayList<String> units) {
        this.units = units;
   } 
    
    

}