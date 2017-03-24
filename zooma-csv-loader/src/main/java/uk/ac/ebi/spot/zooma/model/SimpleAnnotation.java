package uk.ac.ebi.spot.zooma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.StringJoiner;

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

        StringJoiner semTags = new StringJoiner(",");
        if(semantictag.contains("|")){
            String[] sts = semantictag.split("\\|");
            for(String s : sts){
                semTags.add("\"" + s + "\"");
            }
        } else {
            semTags.add("\"" + semantictag + "\"");
        }

        DateTimeFormatter dashedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(getAnnotationdate(), dashedDateFormatter);
        String string = "{" +
                "\"provenance\" : {" +
                    "\"evidence\" : \"" + getEvidence() + "\"," +
                    "\"annotatedDate\" : \"" + dateTime.toString() + "\"," +
                    "\"accuracy\" : \"" + getAccuracy() + "\"," +
                    "\"generator\" : \"ZOOMA" + "\"," +
                    "\"source\" : { " +
                        "\"name\" : \"" + getName() + "\"," +
                        "\"topic\" : \"" + getTopic() + "\"," +
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
                "\"semanticTag\" : [" + semTags.toString() + "]," +
                "\"property\" : {" +
                    "\"propertyType\" : \"" + getPropertytype() + "\"," +
                    "\"propertyValue\" : \"" + getPropertyvalue() + "\"" +
                "}" +
                "}";

        return string;

//        if(annotationdate.contains("/")){
//            String[] splitD = annotationdate.split("/");
//            String time = splitD[2].split(" ")[1];
//            splitD[2] = splitD[2].split(" ")[0];
//            StringBuilder fixedDate = new StringBuilder();
//            if (splitD[splitD.length - 1].length() == 2) {
//                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append("20").append(splitD[0] + " ").append(time).append(":00");;
//            } else {
//                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append(splitD[0] + " ").append(time).append(":00");
//            }
//            this.annotationdate = fixedDate.toString();
//        }
//
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
