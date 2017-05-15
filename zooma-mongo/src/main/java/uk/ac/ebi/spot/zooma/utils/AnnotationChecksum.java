package uk.ac.ebi.spot.zooma.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.model.mongo.MongoAnnotationProvenance;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 23/03/2017.
 */
@Component
public class AnnotationChecksum {

    private final MessageDigest messageDigest = MongoUtils.generateMessageDigest();

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }


    public String getAnnotationHash(Annotation annotation){
        List<String> idContents = new ArrayList<>();

        idContents.add(annotation.getBiologicalEntities().getBioEntity());
        idContents.add(annotation.getBiologicalEntities().getStudies().getStudy());
        idContents.add(annotation.getProperty().getPropertyType());
        idContents.add(annotation.getProperty().getPropertyValue());
        for (String st : annotation.getSemanticTag()){
            idContents.add(st);
        }

        MongoAnnotationProvenance provenance = annotation.getProvenance();
        idContents.add(provenance.getAnnotator() != null ? provenance.getAnnotator() : "");

        idContents.add(provenance.getAnnotatedDate() != null ? provenance.getAnnotatedDate().toString() : "");
        idContents.add(provenance.getEvidence() != null ? provenance.getEvidence().toString() : "");
        idContents.add(provenance.getSource().getUri() != null ? provenance.getSource().getUri() : "");

        return generateIDFromContent(idContents.toArray(new String[idContents.size()]));

    }

    private String generateIDFromContent(String... contents) {
        boolean hasNulls = false;
        for (String s : contents) {
            if (s == null) {
                hasNulls = true;
                break;
            }
        }
        if (hasNulls) {
            StringBuilder sb = new StringBuilder();
            for (String s : contents) {
                sb.append(s).append(";");
            }
            getLog().error("Attempting to generate new ID from content containing nulls: " + sb.toString());
        }
        synchronized (messageDigest) {
            return MongoUtils.generateHashEncodedID(messageDigest, contents);
        }
    }

}
