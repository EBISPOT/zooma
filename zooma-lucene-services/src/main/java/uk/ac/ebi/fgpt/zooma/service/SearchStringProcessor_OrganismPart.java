

package uk.ac.ebi.fgpt.zooma.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;


/** 
 * This class handles the processing of properties of type "organism part".   
 * @author Jose Iglesias
 * @date 16/08/13         */
public class SearchStringProcessor_OrganismPart implements SearchStringProcessor{

    ArrayList<String> qualifier_OrgPart;  /* qualifier_OrgPart contains all subclasses of "anatomical modifier" (EFO)
                                             and all subclasses of "position" (PATO) */

    
    @Override
    public float getBoostFactor() {
        return 0.9f;
    }
   
    /**
    * Returns true if the property type is equal to organism_part
    * Returns false otherwise.
    */
    @Override
    public boolean canProcess(String searchString, String type) {
        
        if(type!=null && !type.isEmpty()){
            
            type = type.toLowerCase();
            
             
            if(  type.contentEquals("organism_part")  || type.contentEquals("organism part")  || 
                 type.contentEquals("organismpart")         ){

                 return true;

            }
        }
        
        return false; 
    }


    /**
    * Takes a string, looks for qualifiers in the string, 
    * removes them and returns the processed string.
    */
    @Override
    public List<String> processSearchString(String searchString) throws IllegalArgumentException {
        
        ArrayList<String> processedStrings = new ArrayList<String>();
        
        String processedString=searchString.toLowerCase();

        //Remove concentration unit
        for(String qualifier: getQualifier_OrgPart()){
            
            if( processedString.contains(" " + qualifier + " ") || processedString.startsWith(qualifier + " ") || processedString.endsWith(" " + qualifier) ){
                
                processedString = processedString.replaceAll( qualifier, " ");
            }        
        }

        //Remove unnecesary spaces..

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
    * Initializes the array of qualifiers related to organism parts from two files:
    *   One file contains all subclasses of "anatomical modifier" (EFO)
    *   The second one contains all subclasses of "position" (PATO)
    */
    public void init() throws IOException {
        
        qualifier_OrgPart = new ArrayList();

        
        try {

            InputStream bufferFile = this.getClass().getClassLoader().getResourceAsStream("efo_dictionary_qualifier_OrgPart.txt");        

            if(bufferFile!=null){

               String stringFile = inputStream_To_String(bufferFile,1000);

               if(stringFile!=null && !stringFile.isEmpty()){

                   String line;

                   String[] lines = stringFile.split("\n");

                   for(int i=0; i<lines.length ; i++){

                      line = lines[i];
                      String[] fields = line.split("\t");  

                      if(fields.length>2){
                           qualifier_OrgPart.add(fields[0]);
                      }
                   }
               }
            }
        

      }catch(Exception e){
         e.printStackTrace();
         
      }
 
      try {
            
            InputStream bufferFile = this.getClass().getClassLoader().getResourceAsStream("pato_dictionary_qualifier_OrgPart.txt");        

            if(bufferFile!=null){

               String stringFile = inputStream_To_String(bufferFile,16000);

               if(stringFile!=null && !stringFile.isEmpty()){

                   String line;

                   String[] lines = stringFile.split("\n");

                   for(int i=0; i<lines.length ; i++){

                      line = lines[i];
                      String[] fields = line.split("\t");  

                      if(fields.length>2){
                           qualifier_OrgPart.add(fields[0]);
                      }
                   }
               }
            }
        

      }catch(Exception e){
         e.printStackTrace();
         
      }  
      System.out.println(qualifier_OrgPart.size() + " qualifiers read from file");

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
    
    
   public ArrayList<String> getQualifier_OrgPart() {
        return qualifier_OrgPart;
   }

    public void setQualifier_OrgPart(ArrayList<String> qualifier_OrgPart) {
        this.qualifier_OrgPart = qualifier_OrgPart;
   } 
    
    

}