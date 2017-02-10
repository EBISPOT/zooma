package uk.ac.ebi.spot.zooma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private boolean batchload;

    public SimpleAnnotation buildAnnotationFromMap(Map<String, String> map){
        SimpleAnnotation annotationT = SimpleAnnotation.builder().bioentity(map.get("bioentity"))
                .study(map.get("study"))
                .propertytype(map.get("propertytype"))
                .propertyvalue(map.get("propertyvalue"))
                .semantictag(map.get("semantictag"))
                .uri(map.get("uri"))
                .name(map.get("name"))
                .topic(map.get("topic"))
                .evidence(map.get("evidence"))
                .accuracy(map.get("accuracy"))
                .generator(map.get("generator"))
                .annotator(map.get("annotator"))
                .annotationdate(map.get("annotationdate"))
                .build();
        if (map.get("batchload") == "true"){
            annotationT.setBatchload(true);
        } else {
            annotationT.setBatchload(false);
        }
        return annotationT;
    }

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

        if(annotationdate.contains("/")){
            String[] splitD = annotationdate.split("/");
            String time = splitD[2].split(" ")[1];
            splitD[2] = splitD[2].split(" ")[0];
            StringBuilder fixedDate = new StringBuilder();
            if (splitD[splitD.length - 1].length() == 2) {
                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append("20").append(splitD[0] + " ").append(time).append(":00");;
            } else {
                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append(splitD[0] + " ").append(time).append(":00");
            }
            this.annotationdate = fixedDate.toString();
        }

        String string = "{" +
                "\"provenance\" : {" +
                "\"evidence\" : \"" + evidence + "\"," +
                "\"annotationDate\" : \"" + annotationdate + "\"," +
                "\"accuracy\" : \"" + accuracy + "\"," +
                "\"generator\" : \"ZOOMA" + "\"," +
                "\"source\" : { " +
                "\"name\" : \"" + name + "\"," +
                "\"topic\" : \"" + topic + "\"," +
                "\"type\" : \"" + type + "\"," +
                "\"uri\" : \"" + uri + "\"" +
                "}," +
                "\"annotator\" : \"" + annotator + "\"" +
                "}," +
                "\"biologicalEntities\" : {" +
                "\"studies\" : {" +
                "\"study\" : \"" + study + "\"" +
                "}," +
                "\"bioEntity\" : \"" + bioentity + "\"" +
                "}," +
                "\"semanticTag\" : [" + semTags.toString() + "]," +
                "\"property\" : {" +
                "\"propertyType\" : \"" + propertytype + "\"," +
                "\"propertyValue\" : \"" + propertyvalue + "\"" +
                "}" +
                "}";

        return string;
    }
}