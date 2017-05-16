package uk.ac.ebi.spot.zooma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleAnnotation {
    private String annotationid;
    private String bioentity;
    private String study;
    private String propertytype;
    private String propertyvalue;
    private String semantictag;
    private String uri;
    private String name;
    private final String type = "DATABASE";
    private String topic;
    private String evidence;
    private String accuracy;
    private String generator;
    private String annotator;
    private String annotationdate;
    private Action action;


    @Override
    public String toString() {
        Collection<String> semanticTags = this.getStringAsCollection(getSemantictag());
        Collection<String> topics = this.getStringAsCollection(getTopic());
        String string = "{" +
                "\"provenance\" : {" +
                    "\"evidence\" : \"" + getEvidence() + "\"," +
                    "\"annotatedDate\" : \"" + getAnnotationdate() + "\"," +
                    "\"accuracy\" : \"" + getAccuracy() + "\"," +
                    "\"generator\" : \"ZOOMA" + "\"," +
                    "\"source\" : { " +
                        "\"name\" : \"" + getName() + "\"," +
                        "\"topic\" : " + topics.toString() + "," +
                        "\"type\" : \"" + getType() + "\"," +
                        "\"uri\" : \"" + getUri() + "\"" +
                    "}," +
                    "\"annotator\" : \"" + getAnnotator() + "\"" +
                "}," +
                "\"biologicalEntities\" : {" +
                    "\"studies\" : {" +
                        "\"study\" : \"" + getStudy() + "\"" +
                    "}," +
                    "\"bioEntity\" : \"" + getBioentity() + "\"" +
                "}," +
                "\"semanticTag\" : " + semanticTags.toString() + "," +
                "\"property\" : {" +
                    "\"propertyType\" : \"" + getPropertytype() + "\"," +
                    "\"propertyValue\" : \"" + getPropertyvalue() + "\"" +
                "}" +
                "}";

        return string;

    }

    private Collection<String> getStringAsCollection(String string){
        Collection<String> collection = new ArrayList<>();
        if(string.contains("|")){
            String[] sts = string.split("\\|");
            for(String s : sts){
                collection.add("\"" + s + "\"");
            }
        } else {
            collection.add("\"" + string + "\"");
        }
        return collection;
    }

   public enum Action {

       CREATED,
       REPLACED,
       ALREADY_EXISTS,
       UNKNOWN;

       public static Action lookup(String id) {
           for (Action e : Action.values()) {
               if (e.name().equals(id)) {
                   return e;
               }
           }
           return Action.UNKNOWN;
       }
   }
}
